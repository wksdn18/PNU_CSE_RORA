# -*- coding: utf-8 -*-
# Inception-v3 ���� �̿��� Image Classification

# ���� ����Ʈ ����
from __future__ import absolute_import
from __future__ import division
from __future__ import print_function

# �ʿ��� ���̺귯������ ����Ʈ
import os.path
import re
import sys
import tarfile
from picamera import PiCamera
from time import sleep
import numpy as np
from six.moves import urllib
from subprocess import call
import tensorflow as tf

FLAGS = tf.app.flags.FLAGS

# classify_image_graph_def.pb:
#   GraphDef protocol buffer�� ���� ǥ��
# imagenet_synset_to_human_label_map.txt:
#   synset ID�� �ΰ��� ������ �ִ� ���ڷ� ����
# imagenet_2012_challenge_label_map_proto.pbtxt:
#   protocol buffer�� ���� ǥ���� synset ID�� ���̺�� ����

# Inception-v3 ���� �ٿ�ε� ���� ��θ� ����
tf.app.flags.DEFINE_string(
    'model_dir', './model',
    """Path to classify_image_graph_def.pb, """
    """imagenet_synset_to_human_label_map.txt, and """
    """imagenet_2012_challenge_label_map_proto.pbtxt.""")
# ���� �̹��� ������ ��θ� ����
tf.app.flags.DEFINE_string('image_file', '',
                           """Absolute path to image file.""")
# �̹����� �߷а���� ����� ǥ���� ������ ����
tf.app.flags.DEFINE_integer('num_top_predictions', 5,
                            """Display this many predictions.""")

# Inception-v3 ���� �ٿ�ε��� URL �ּ�
DATA_URL = 'http://download.tensorflow.org/models/image/imagenet/inception-2015-12-05.tgz'

# ���� ������ node ID�� �ΰ��� ������ �� �ִ� ���̺�� ��ȯ
class NodeLookup(object):

  def __init__(self,
               label_lookup_path=None,
               uid_lookup_path=None):
    if not label_lookup_path:
      label_lookup_path = os.path.join(
          FLAGS.model_dir, 'imagenet_2012_challenge_label_map_proto.pbtxt')
    if not uid_lookup_path:
      uid_lookup_path = os.path.join(
          FLAGS.model_dir, 'imagenet_synset_to_human_label_map.txt')
    self.node_lookup = self.load(label_lookup_path, uid_lookup_path)

  def load(self, label_lookup_path, uid_lookup_path):
    """������ softmax node�� ���� �ΰ��� ���� �� �ִ� ���� �ܾ �ε� ��.
    Args:
      label_lookup_path: ���� node ID�� ���� ���� UID.
      uid_lookup_path: �ΰ��� ���� �� �ִ� ���ڿ� ���� ���� UID.
    Returns:
      ���� node ID�κ��� �ΰ��� ���� �� �ִ� ���ڿ� ���� dict.
    """
    if not tf.gfile.Exists(uid_lookup_path):
      tf.logging.fatal('File does not exist %s', uid_lookup_path)
    if not tf.gfile.Exists(label_lookup_path):
      tf.logging.fatal('File does not exist %s', label_lookup_path)

    #  ���� UID�κ��� �ΰ��� ���� �� �ִ� ���ڷ��� ������ �ε���.
    proto_as_ascii_lines = tf.gfile.GFile(uid_lookup_path).readlines()
    uid_to_human = {}
    p = re.compile(r'[n\d]*[ \S,]*')
    for line in proto_as_ascii_lines:
      parsed_items = p.findall(line)
      uid = parsed_items[0]
      human_string = parsed_items[2]
      uid_to_human[uid] = human_string

    # ���� UID�κ��� ���� node ID�� ���� ������ �ε���.
    node_id_to_uid = {}
    proto_as_ascii = tf.gfile.GFile(label_lookup_path).readlines()
    for line in proto_as_ascii:
      if line.startswith('  target_class:'):
        target_class = int(line.split(': ')[1])
      if line.startswith('  target_class_string:'):
        target_class_string = line.split(': ')[1]
        node_id_to_uid[target_class] = target_class_string[1:-2]

    # ���������� ���� node ID�κ��� �ΰ��� ���� �� �ִ� ���ڷ��� ������ �ε���.
    node_id_to_name = {}
    for key, val in node_id_to_uid.items():
      if val not in uid_to_human:
        tf.logging.fatal('Failed to locate: %s', val)
      name = uid_to_human[val]
      node_id_to_name[key] = name

    return node_id_to_name

  def id_to_string(self, node_id):
    if node_id not in self.node_lookup:
      return ''
    return self.node_lookup[node_id]


def create_graph():
  """����� GraphDef ���Ϸκ��� �׷����� �����ϰ� ����� ���� ������."""
  # Creates graph from saved graph_def.pb.
  with tf.gfile.FastGFile(os.path.join(
      FLAGS.model_dir, 'classify_image_graph_def.pb'), 'rb') as f:
    graph_def = tf.GraphDef()
    graph_def.ParseFromString(f.read())
    _ = tf.import_graph_def(graph_def, name='')


def run_inference_on_image(image):
  """�̹����� ���� �߷��� ����
  Args:
    image: �̹��� ���� �̸�.
  Returns:
    ����(Nothing)
  """
  if not tf.gfile.Exists(image):
    tf.logging.fatal('File does not exist %s', image)
  image_data = tf.gfile.FastGFile(image, 'rb').read()

  # ����� GraphDef�κ��� �׷��� ����
  create_graph()

  with tf.Session() as sess:
    # ��� ������ �ټ���:
    # 'softmax:0': 1000���� ���̺� ���� ����ȭ�� ���������(normalized prediction)�� �����ϰ� �ִ� �ټ�   
    # 'pool_3:0': 2048���� �̹����� ���� float ���縦 �����ϰ� �ִ� next-to-last layer�� �����ϰ� �ִ� �ټ�
    # 'DecodeJpeg/contents:0': ������ �̹����� JPEG ���ڵ� ���ڸ� �����ϰ� �ִ� �ټ�

    # image_data�� ��ǲ���� graph�� ����ְ� softmax tesnor�� �����Ѵ�.
    softmax_tensor = sess.graph.get_tensor_by_name('softmax:0')
    predictions = sess.run(softmax_tensor,
                           {'DecodeJpeg/contents:0': image_data})
    predictions = np.squeeze(predictions)

    # node ID --> ���� �ܾ� lookup�� �����Ѵ�.
    node_lookup = NodeLookup()
    f = open('object.txt','w')
    flag=0
    top_k = predictions.argsort()[-FLAGS.num_top_predictions:][::-1]
    for node_id in top_k:
      human_string = node_lookup.id_to_string(node_id)
      score = predictions[node_id]
      print('%s (score = %.5f)' % (human_string, score))
      if( flag<3):
        f.write(human_string)
        flag=flag+1
    f.close()

def maybe_download_and_extract():
  """Download and extract model tar file."""
  dest_directory = FLAGS.model_dir
  if not os.path.exists(dest_directory):
  	os.makedirs(dest_directory)
  filename = DATA_URL.split('/')[-1]
  filepath = os.path.join(dest_directory, filename)
  if not os.path.exists(filepath):
    def _progress(count, block_size, total_size):
      sys.stdout.write('\r>> Downloading %s %.1f%%' % (
          filename, float(count * block_size) / float(total_size) * 100.0))
      sys.stdout.flush()
    filepath, _ = urllib.request.urlretrieve(DATA_URL, filepath, _progress)
    print()
    statinfo = os.stat(filepath)
    print('Succesfully downloaded', filename, statinfo.st_size, 'bytes.')
  tarfile.open(filepath, 'r:gz').extractall(dest_directory)

  
def main(argv=None):
  # Inception-v3 ���� �ٿ�ε��ϰ� ������ Ǭ��.
  maybe_download_and_extract()
  camera = PiCamera()
  camera.resolution = (640,640)
  camera.start_preview()
  camera.brightness = 55
  sleep(0.1)
  camera.capture('object.jpg')
  camera.stop_preview()
  #call(['raspistill','-t','1','-w','640','-h','640','-br','55','-o','object.jpg'])
  # ��ǲ���� �Է��� �̹����� �����Ѵ�.
  image = (FLAGS.image_file if FLAGS.image_file else
           os.path.join('object.jpg'))

  # ��ǲ���� �ԷµǴ� �̹����� ���� �߷��� �����Ѵ�.
  run_inference_on_image(image)


if __name__ == '__main__':
  tf.app.run()
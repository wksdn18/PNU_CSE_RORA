# -*- coding: utf-8 -*-

"""Inception v3 architecture 모델을 retraining한 모델을 이용해서 이미지에 대한 추론(inference)을 진행하는 예제"""

from time import sleep
from picamera import PiCamera
import numpy as np
import tensorflow as tf
import cv2
import sys
import subprocess
                                     # 추론을 진행할 이미지 경로
modelFullPath = '/home/pi/face/output_graph.pb'                                      # 읽어들일 graph 파일 경로
labelsFullPath = '/home/pi/face/output_labels.txt'                                   # 읽어들일 labels 파일 경로



def create_graph():
    """저장된(saved) GraphDef 파일로부터 graph를 생성하고 saver를 반환한다."""
    # 저장된(saved) graph_def.pb로부터 graph를 생성한다.
    with tf.gfile.FastGFile(modelFullPath, 'rb') as f:
        graph_def = tf.GraphDef()
        graph_def.ParseFromString(f.read())
        _ = tf.import_graph_def(graph_def, name='')


def run_inference_on_image():
    camera = PiCamera()
    camera.resolution = (640,640)
    camera.start_preview()
    camera.brightness = 55
    sleep(1)
    camera.capture('image.jpg')
    camera.stop_preview()
    # 저장된(saved) GraphDef 파일로부터 graph를 생성한다.
    create_graph()

	
    face_cascade = cv2.CascadeClassifier('haarcascade_frontalface_default.xml')
    eye_casecade = cv2.CascadeClassifier('haarcascade_eye.xml')

    img = cv2.imread('image.jpg')
    gray = cv2.imread('image.jpg',0)
    faces = face_cascade.detectMultiScale(gray, 1.3,5)

    for (x,y,w,h) in faces:
        cropped = img[y - int(y/5) :y + h + int(y/10) , x - int(x/10) :x + w + int(x/10)]
        cv2.imwrite("test.jpg", cropped)

    answer = None
    
    imagePath = "test.jpg"
    if not tf.gfile.Exists(imagePath):
        tf.logging.fatal('File does not exist %s', imagePath)
        return answer

    image_data = tf.gfile.FastGFile(imagePath, 'rb').read()

    with tf.Session() as sess:

        softmax_tensor = sess.graph.get_tensor_by_name('final_result:0')
        predictions = sess.run(softmax_tensor,
                               {'DecodeJpeg/contents:0': image_data})
        predictions = np.squeeze(predictions)

        top_k = predictions.argsort()[-5:][::-1]  # 가장 높은 확률을 가진 5개(top 5)의 예측값(predictions)을 얻는다.
        f = open(labelsFullPath, 'rb')
        lines = f.readlines()
        labels = [str(w).replace("\n", "") for w in lines]
        for node_id in top_k:
            human_string = labels[node_id]
            score = predictions[node_id]
            print('%s (score = %.5f)' % (human_string, score))
        f = open('t.txt','w')
        answer = labels[top_k[0]]
        f.write(answer)
        f.close()
        
        return answer


if __name__ == '__main__':
    sys.exit(run_inference_on_image())

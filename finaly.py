import picamera
import time
import smtplib
from email.MIMEMultipart import MIMEMultipart
from email.MIMEText import MIMEText
from email.MIMEBase import MIMEBase
from email import encoders
import datetime
import cv2
from sklearn.externals import joblib
from skimage.feature import hog
import numpy as np
from time import sleep

#create object for PiCamera class
camera = picamera.PiCamera()
#set resolution
camera.resolution = (1024, 768)
camera.brightness = 60
camera.start_preview()
#add text on image
sleep(5)
#store image
camera.capture('/home/pi/Downloads/10.jpg')
camera.stop_preview()
time.sleep(5)

# Load the classifier
clf = joblib.load(r"/home/pi/Downloads/digitTrainingData_protocol2.pkl")

# Read the input image 
testImg = cv2.imread(r"/home/pi/Downloads/4.jpg")

# Convert to grayscale and apply Gaussian filtering
imgGray = cv2.cvtColor(testImg, cv2.COLOR_BGR2GRAY)
imgGray = cv2.GaussianBlur(imgGray, (5, 5), 0)

# Threshold the image
ret, thresholdedImg = cv2.threshold(imgGray, 90, 255, cv2.THRESH_BINARY_INV)

# Find contours in the image
ctrs, hier = cv2.findContours(thresholdedImg.copy(), cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)


# Get rectangles contains each contour
rects = [cv2.boundingRect(ctr) for ctr in ctrs]
predictedNumbers = []


# For each rectangular region, calculate HOG features and predict
# the digit using Linear SVM.
for rect in rects:
    # Draw the rectangles
    cv2.rectangle(testImg, (rect[0], rect[1]), (rect[0] + rect[2], rect[1] + rect[3]), (0, 255, 0), 3)
    #print("width "+str(tteemmpp.width)+" length "+str(tteemmpp.length))
    # Make the rectangular region around the digit
    leng = int(rect[3] * 1.6)
    pt1 = int(rect[1] + rect[3] // 2 - leng // 2)
    pt2 = int(rect[0] + rect[2] // 2 - leng // 2)
    roi = thresholdedImg[pt1:pt1+leng, pt2:pt2+leng]
    # Resize the image
    roi = cv2.resize(roi, (128, 128), interpolation=cv2.INTER_AREA)
    roi = cv2.dilate(roi, (3, 3))
    # Calculate the HOG features
    roi_hog_fd = hog(roi, orientations=9, pixels_per_cell=(14, 14), cells_per_block=(1, 1), visualize=False)
    nbr = clf.predict(np.array([roi_hog_fd], 'float64'))
    predictedNumbers.append(nbr[0])
    cv2.putText(testImg, str(int(nbr[0])), (rect[0], rect[1]),cv2.FONT_HERSHEY_DUPLEX, 2, (255, 0, 0), 3)


cv2.imshow("Resulting Image with Rectangular ROIs", testImg)

minPredictedNum = min(predictedNumbers)


fromaddr = "waterlevel.nesac@gmail.com"
toaddr = "waterlevel.nesac@gmail.com"
 
msg = MIMEMultipart()
 
msg['From'] = fromaddr
msg['To'] = toaddr
msg['Subject'] = "Waterlevel reports for Dam"

time = datetime.datetime.now()
body = """\
<html>
  <head></head>
  <body>
    <p>Waterlevel : """+ str(minPredictedNum)+""" <br>
       Date and Time : """+ str(time)+""" <br>
    </p>
  </body>
</html>
"""
 
msg.attach(MIMEText(body, 'html'))
 
filename = "4.jpg"
attachment = open("/home/pi/Downloads/4.jpg", "rb")
 
part = MIMEBase('application', 'octet-stream')
part.set_payload((attachment).read())
encoders.encode_base64(part)
part.add_header('Content-Disposition', "attachment; filename= %s" % filename)
 
msg.attach(part)
 
server = smtplib.SMTP('smtp.gmail.com', 587)
server.starttls()
server.login(fromaddr, "water_level")
text = msg.as_string()
server.sendmail(fromaddr, toaddr, text)
server.quit()

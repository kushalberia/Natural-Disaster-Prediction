# Natural-Disaster-Prediction
The idea is to design a IoT device to "reduce the risk of floods" by early prediction. We built a successful prototype device to constantly keep a check on the Water Levels and hence eliminate the need of physical human checking at river sites. The project is built on a Raspberry Pi 3.0 model B, with just 2 accessories, a Pi-camera and a portable WiFi device. The Pi-Camera is interfaced to take pictures of the water level scale installed on the sites. Making use of Neural Networks the program is trained with a suitable data-set to identify markings on the scale. The values obtained will then be transferred to any host machine at any place via WiFi and from there it is updated on a Geoportal recursively, for public access so that everyone concerned is aware all the time. If the water rises above a certain limit the authorities can be warned. We want to further explore and improvise the functionality by being able to measure the rate of change in water levels so that we can determine the level of seriousness associated with the issue. This can be achieved by using AI technology. The prototype works successfully and we implemented the same for a DAM site in Meghalaya. 

Uploads : The main code running on Raspberry Pi as Fianly.py
          , The result of dataset training as digitalTrainingData.pkl
          , The mail extraction for PC as WLMailHandler.java

Link of Presentation : https://docs.google.com/presentation/d/1EB6dllRygAnB7k9BXlR8nMBQ1QHSMonVL6idKwNlZQk/edit?usp=sharing

Link of Video : https://youtu.be/NJALxBqN-vk

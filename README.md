# Модуль по визуализации
## Сборка проекта
[![Build Status](https://travis-ci.org/Snezzz/youtube_data_api.svg?branch=master)](https://travis-ci.org/Snezzz/youtube_data_api)
[![Build Status](https://ci.appveyor.com/api/projects/status/github/Snezzz/youtube_data_api)](https://ci.appveyor.com/api/projects/status/github/Snezzz/youtube_data_api)  

### Системные требования:
  * OS: Windows x86/x86_64, Linux x86/x86_64  
  * Оперативная память (RAM): 13n Free RAM, где n - минимальное количество видео в запросе  
  * Для сборки: версия Java не ниже 1.8
   
### Сборка из исходников:  
1.Установите Java  
2.Установите Apache Maven (http://maven.apache.org)  
3.В терминале: ```mvn compile```  
4.```mvn assembly:single```  


## Описание модуля
  Модуль отвечает за получение данных из БД и выбор варианта построения графа.
  
  ## Визуализация (k - мин. степень вершин)
  ##### OpenOrd, Modularity, k=1
  ![Screenshot](results/3.jpg)
  ##### OpenOrd, Modularity, k=3
   ![Screenshot](results/4.jpg)
  ##### Yifan Hu, Modularity, k=6
   ![Screenshot](results/5.jpg)
  ##### OpenOrd, Modularity, k=8
   ![Screenshot](results/6.jpg)
   ##### Yifan Hu, PageRank k=6
   ![Screenshot](results/7.jpg)
   ##### Modularity
   ![Screenshot](results/1.jpg)
   ##### ForceAtlas2, special graph
   ![Screenshot](results/2.png)
  ##### OpenOrd, Modularity, k=6
   ![Screenshot](results/14.jpg)
   ##### OpenOrd, Modularity, k=10
   ![Screenshot](results/15.jpg)
   ##### OpenOrd, Modularity, k=12
   ![Screenshot](results/16.jpg)

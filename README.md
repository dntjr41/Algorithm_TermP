## Algorithm Term Project - Team 1

## Gachon Castle (가천성[사천성])
## Presentation - [Final_Team1 심우석, 임인범, 박준영.pptx](https://github.com/dntjr41/Algorithm_TermP/files/7673310/Final_Team1.pptx)

***

1. Descrption
2. Algorithm
3. Demo
4. Role

***
# 1. Description

## Gachon Castle (가천성)
First, 5 * 5 line up the cards. The method of listing is irregular random.

The cases where the cards can be removed are as follows.

1. When two same cards are adjacent to each other.
2. If there are different cards between two cards of the same shape, <br> when the number of times the line is bent by connecting them in a horizontal or vertical straight line is within two times

Repeat the above method to remove the cards.
If all cards are removed, the game will be over.

※ Depending on the list of cards, there are many cases where the game cannot be solved
-> We solved this problem. <br>

* Development Environment <br>
Java - ![image](https://user-images.githubusercontent.com/67234937/145147481-46f47c89-cdfc-431a-8c33-939d71fbf6c1.png) IntelliJ - ![image](https://user-images.githubusercontent.com/67234937/145147514-ef11f2d3-36f2-46fb-9d28-e055f44ec19f.png)

* 5 X 5 board Table <br>
![image](https://user-images.githubusercontent.com/67234937/145147634-441eb396-5353-49fd-b536-a1c8431b14e1.png) -> ![image](https://user-images.githubusercontent.com/67234937/145147638-4e53f97b-eaaf-4ff5-b2ca-ab232fa813ad.png)

* Block Image (Things related to Gachon University) <br>
![image](https://user-images.githubusercontent.com/67234937/145147778-da2d2ac1-781e-40ba-a9f3-e68b5d96e4ad.png) -> ![image](https://user-images.githubusercontent.com/67234937/145147787-5ba7b0dc-6b61-4a04-a518-eb407cde60c1.png) <br>
![block_1](https://user-images.githubusercontent.com/67234937/145147902-3ad02307-3b4c-48e6-85c2-b84051cbc30b.png)
![block_2](https://user-images.githubusercontent.com/67234937/145147911-94fb05eb-2aba-44ed-875c-444016ade1cb.PNG)
![block_3](https://user-images.githubusercontent.com/67234937/145147917-081077c1-1f69-4b64-8126-05dd7fffe778.PNG)
![block_4](https://user-images.githubusercontent.com/67234937/145147918-7befe2c4-7b1c-4724-8e4d-6de6b535c0c8.png)

* Sound Effect <br>
![image](https://user-images.githubusercontent.com/67234937/145147992-aaf33a50-0bf8-4c45-ba53-0353795cbcb1.png)
* Click (Correct) -> https://user-images.githubusercontent.com/67234937/145148532-c952ded2-1d23-4e42-9808-a32de1e8ffe5.mp4
* Click (Incorrect) -> https://user-images.githubusercontent.com/67234937/145148564-c6ce449c-fb32-4fd3-b1b8-9530db77da24.mp4
* Win -> https://user-images.githubusercontent.com/67234937/145148588-58ecfbbb-f018-488f-b92a-e6ece3b5ffdc.mp4

* Start <br>
![image](https://user-images.githubusercontent.com/67234937/145148683-fe4a0be0-c947-45d9-bc3d-a82702e8e119.png)

* Play <br>
![image](https://user-images.githubusercontent.com/67234937/145148698-71a89a83-f733-4bbd-a0d4-0b1f02fd21f5.png)

* Click <br>
![image](https://user-images.githubusercontent.com/67234937/145148718-166adbec-7a5d-42ef-8c36-f878c1c3f919.png)

* Win <br>
![image](https://user-images.githubusercontent.com/67234937/145148731-79c35ae9-d7fb-44b9-b3f3-c08f68d3d7cc.png)

*** 
# 2. Algorithm

* The classes implemented by using JAVA <br>
<MainFrame.java> <br>
Set constants for the game, etc. <br>
Duplicate image and save background as tile. <br>
Set default game environment. (window, variable) <br>
Implement the main logic of the game.

* Start - ![image](https://user-images.githubusercontent.com/67234937/145149174-482e3f56-a93f-4f06-ba7b-4f51c98fbbac.png)
* Playing - ![image](https://user-images.githubusercontent.com/67234937/145149188-320ae1c8-ec59-484b-9b06-d6b69213bc26.png)
* Playing (incorrect) - If two blocks are selected, then we have to check whether the blocks are deletable. <br>
![image](https://user-images.githubusercontent.com/67234937/145149226-ee281f6b-a27e-40cb-9951-1b949e0afaeb.png) <br>
![image](https://user-images.githubusercontent.com/67234937/145149234-aa2c8cd8-a93d-4d62-8ba5-1fd49a124eb8.png)

* Playing (correct) - If two selected blocks are different, then check whether <br> 
the blocks are deletable by using checkBlockCol function. <br>
![image](https://user-images.githubusercontent.com/67234937/145149377-e2dffd4c-158f-4356-977c-72459ee91fbe.png)

###  Using the Backtracking (Modified DFS)
* Check all paths to see If there are partner card <br>
Linear       - ( │ , ─ ) <br>
Non Linear   - ( ┌, ┐, └, ┘) 

* Check Block Col <br>
![image](https://user-images.githubusercontent.com/67234937/145149562-d2d0d6e7-1e31-435e-8555-c5c9184cb747.png) <br>
![image](https://user-images.githubusercontent.com/67234937/145149568-30ceadbf-a893-4fc4-a370-3d717b8dac17.png) <br>
![image](https://user-images.githubusercontent.com/67234937/145149600-92af1f63-dede-48dd-9083-091393a1d228.png)

* Check Block Col Sub1 - Check for linear cases <br>
![image](https://user-images.githubusercontent.com/67234937/145149667-09613dc3-7af3-440d-b539-43bf4f0c9b69.png) <br>
![image](https://user-images.githubusercontent.com/67234937/145149719-34e18512-a737-429c-bebc-daf287e6f5d3.png)

* is Block Already Exist -> Check whether there is a block in location (i, j) <br>
![image](https://user-images.githubusercontent.com/67234937/145149785-38870b95-4322-4419-80c4-7b04c25b831d.png) <br>
![image](https://user-images.githubusercontent.com/67234937/145149802-40f25d15-37d9-46ed-aec0-c1125cf0abf3.png)

* Check Block Col Sub2 - Check for '└' or '┐' cases <br>
![image](https://user-images.githubusercontent.com/67234937/145149913-1639b48d-bff7-42a1-a7f2-44ae37fb4d63.png) <br>
![image](https://user-images.githubusercontent.com/67234937/145149925-e13dd506-ae2b-4a89-ac71-01e08aac047d.png)

* Check Block Col Sub3 - Check for 'ㄷ' cases <br>
![image](https://user-images.githubusercontent.com/67234937/145150007-7e477a89-ac36-421c-9c20-12184e6bf17b.png) <br>
![image](https://user-images.githubusercontent.com/67234937/145150020-4e71d66e-3e12-498e-94f1-ee3cc95e55ed.png)

***
# 3. Demo
### ★ Turn On Sound ★
https://user-images.githubusercontent.com/67234937/145150078-950c1716-be80-4e3f-ac2e-f6bc9aa40e9d.mp4

***
# 4. Member Role
* 심우석 201636417 - qkqh8639@gmail.com <br>
Structure, Algorithm, GUI Implementation, Final PPT, Wiki Page

* 박준영 201835459 - jyp9i7y@gmail.com <br>
Algorithm, GUI Implementation, Final PPT, Presentation

* 임인범 201835509 - dlsdlaqja888@gmail.com <br>
Structure, GUI Implementation, Final ppt, Description

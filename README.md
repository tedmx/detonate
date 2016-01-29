# detonate
Platformer concept with more independent AI.

![](http://i.imgur.com/IXSq6uJ.jpg)

Platformer in which "move" commands are issued by tapping on areas around the map. Because of it, app requires basic pathfinding logics, and one is written for the app. Pathfinding gets more interesting as there are not only stable ground "stone" blocks, but also destructable "light" blocks, on which the player may jump.

When fight commences, controls are simple - player just has to be agile and pick correct place on the screen to tap on, thus picking suiting attack/defense action.

A few tricky `Canvas` optimizations are used for better performance.

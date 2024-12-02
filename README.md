## cpsc219_project Name: Space Invaders Project

## Game Description
The objective of this game is to survive as long as possible/get the highest number of points possible and kill all the aliens. You play as a ship that is shooting at a descending group of aliens to prevent them from shooting at the player or reaching the bottom of the screen.

## Game Structure
1. Screen
    a) Start screen
        Displays the start menu, instructions, and handles input to start the game.
    b) Main game screen
        Displays the main gameplay interface, manages the background, and calls updates for game objects.
        i) Images
        ii) Background scroll
        A game loop runs continuously while isGameOver is false.
        In each loop iteration:
            Check variable values: Updates are made to game states (player health, enemy count, etc).
            Update display: Game objects (player, enemies, bullets) are redrawn on the screen.
            Process inputs: Player controls (movement, shooting) are read and executed.
            Check winning or loss states: If conditions are met (all enemies defeated or player health is equal to 0), isGameOver is set to true, ending the loop.
2. Plane - parent class
    a) Player plane 
        Attributes:
            i) Control
            ii) Shooting
            iii) Health
    b) Enemy/alien/boss planes
        Attributes:
            i) Movement
            ii) Automatic shooting
            iii) Health and vanishing
3. Bullet - parent class
    a) Player bullet
        If hits enemy plane, the enemy plane disappears
        If hits boss, the boss' health bar decreases
    b) Enemy bullet
        If hits player plane, the player's health bar decreases by 1
    c) Boss bullet
        If hits player plane, the player's health bar decreases by >1


## How to run this game
On the left sidebar, select Search or go to and find your project. Above the file list, select Code. From the options, select the files you want to download (all). Open the code in your java IDE and run the main file (class gameInitialize). Follow the game instructions on the Start Screen to begin running the game.

/*
        Project title: Tetris
        Author: Jakub Sobczyński
        GitHub: https://github.com/J-Sob
 */

package com.company.tetris;


import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;

public class Tetris extends Application {       // class containing game loop and displaying game on screen
    Engine gameEngine;
    Group root;
    Scene scene;
    Canvas stackLayer;
    Canvas dynamicElementsLayer;
    Canvas grid;
    GraphicsContext gcStack;
    GraphicsContext gcDynamicElems;
    GraphicsContext gcGrid;
    double tileWidth;
    double tileHeight;
    final Color fallingPiece = Color.rgb(0, 163, 14);
    final Color stackPiece = Color.rgb(0, 50, 163);
    final Color background = Color.rgb(224, 224, 224);

    public Tetris(){        // preparing scene to display game
        root = new Group();
        scene = new Scene(root,500,500);
        stackLayer = new Canvas(scene.getWidth() / 2,scene.getHeight());
        dynamicElementsLayer = new Canvas(scene.getWidth(), scene.getHeight());
        grid = new Canvas(scene.getWidth() / 2, scene.getHeight());
        root.getChildren().add(stackLayer);
        root.getChildren().add(dynamicElementsLayer);
        root.getChildren().add(grid);
        gcStack = stackLayer.getGraphicsContext2D();
        gcDynamicElems = dynamicElementsLayer.getGraphicsContext2D();
        gcGrid = grid.getGraphicsContext2D();
        gameEngine = new Engine();
        this.tileWidth = scene.getWidth() / gameEngine.getBoardRows();      // tile size depends on window size
        this.tileHeight = this.tileWidth;
    }


    void playTetris(){      // main loop made with JavaFX AnimationTimer
        new AnimationTimer(){
            long update = 0;
            @Override
            public void handle(long now) {
                scene.setOnKeyPressed(keyEvent -> keyboardInput(keyEvent));
                if(!gameEngine.isGameOver() && now - update >= 600_000_000){    // calculating if 0.6 second has passed to move a piece down
                    gameEngine.lowerTetromino();
                    update = now;
                }
                gameEngine.checkBoard();
                draw();
                if(gameEngine.isGameOver()){
                    gameOverWindow(this);
                    this.stop();
                }
            }
        }.start();
    }

    void clearCanvas(){     // clearing canvas before drawing on it
        gcStack.clearRect(0,0, stackLayer.getWidth(), stackLayer.getHeight());
        gcDynamicElems.clearRect(0,0, dynamicElementsLayer.getWidth(), dynamicElementsLayer.getHeight());
    }

    void draw(){        // draws game along with gui on screen
        clearCanvas();
        drawStack();
        drawDynamicElements();
    }

    void drawStack(){       // draws only fallen static pieces and background
        for(int i = 0; i < gameEngine.getBoardRows(); i++){
            for(int j = 0; j < gameEngine.getBoardColumns(); j++){
                if(gameEngine.getTileStatus(i, j)){
                    gcStack.setFill(stackPiece);
                }else{
                    gcStack.setFill(background);
                }
                gcStack.fillRect(j * tileWidth,i * tileHeight, tileWidth, tileHeight);
            }
        }
    }

    void drawDynamicElements(){     // draws all elements that change during game along with Labels
        // drawing falling tiles
        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                if(gameEngine.tetro.getTetroPosition(i,j)){
                    int realColumnPos = gameEngine.tetro.getColumnPos() + j;
                    int realRowPos = gameEngine.tetro.getRowPos() + i;
                    gcDynamicElems.setFill(fallingPiece);
                    gcDynamicElems.fillRect(realColumnPos * tileWidth,realRowPos * tileHeight, tileWidth, tileHeight);
                }
                // drawing upcoming tile
                if(gameEngine.upcomingTetro.getTetroPosition(i,j)){
                    gcDynamicElems.setFill(fallingPiece);
                    gcDynamicElems.fillRect(j * tileWidth + (scene.getWidth() * 0.65), tileHeight * i + (scene.getHeight() * 0.1), tileWidth, tileHeight);
                }
            }
        }
        // Text //
        gcDynamicElems.setFont(Font.loadFont("file:src/resources/Bubblegum.ttf", 30));
        // Upcoming tile label
        gcDynamicElems.strokeText("Next block: ",scene.getWidth() * 0.6, scene.getHeight() * 0.08);
        // Score
        gcDynamicElems.strokeText("Score:",scene.getWidth() * 0.6, scene.getHeight() * 0.3);
        gcDynamicElems.strokeText(String.valueOf(gameEngine.getScore()),scene.getWidth() * 0.6, scene.getHeight() * 0.38);
        //Game Over
        if(gameEngine.isGameOver()){
            gcDynamicElems.strokeText("Game over!",scene.getWidth() * 0.6, scene.getHeight() * 0.5);
        }
    }



    void drawGrid(){        // drawing grid over game board
        gcGrid.setLineWidth(1.0);
        gcGrid.setFill(Color.BLACK);
        for(int i = 0; i < gameEngine.getBoardColumns(); i++){
            gcGrid.moveTo(i * tileWidth, 0);
            gcGrid.lineTo(i * tileWidth, grid.getHeight());
            gcGrid.stroke();
        }

        for(int i = 0; i < gameEngine.getBoardRows(); i++){
            gcGrid.moveTo(0, i * tileHeight);
            gcGrid.lineTo(grid.getWidth(),i * tileHeight);
            gcGrid.stroke();
        }
    }

    void keyboardInput(KeyEvent key){       // menaging user input
        if(!gameEngine.isGameOver()){
            switch(key.getCode()){
                case RIGHT -> gameEngine.moveTetromino(Engine.Direction.RIGHT);
                case LEFT -> gameEngine.moveTetromino(Engine.Direction.LEFT);
                case DOWN -> gameEngine.moveTetromino(Engine.Direction.DOWN);
                case UP -> gameEngine.rotateTetromino();
            }
        }
    }

    void gameOverWindow(AnimationTimer timer){      // creating window popup after lost game
        Font font = Font.loadFont("file:src/resources/Bubblegum.ttf",30);
        GridPane overRoot = new GridPane();
        Scene overScene = new Scene(overRoot,300,200);
        Stage overStage = new Stage();
        overRoot.getColumnConstraints().add(new ColumnConstraints(50));
        overRoot.getColumnConstraints().add(new ColumnConstraints(200));
        overRoot.getColumnConstraints().add(new ColumnConstraints(50));
        overRoot.setVgap(20);
        overRoot.setHgap(4);

        Label gameOverLabel = new Label("GAME OVER!");
        gameOverLabel.setFont(font);
        overRoot.add(gameOverLabel,1,1);

        TextField nickname = new TextField("Enter your name here");
        nickname.setAlignment(Pos.CENTER);
        overRoot.add(nickname,1,2);


        GridPane buttonsGrid = new GridPane();
        buttonsGrid.setHgap(30);

        Button save = new Button("Save your score");
        buttonsGrid.add(save,0,1);

        save.setOnAction(actionEvent -> {
            try {
                gameEngine.saveScore(nickname.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        Button restart = new Button("Play again");
        buttonsGrid.add(restart,1,1);

        restart.setOnAction(actionEvent -> {
            gameEngine.restartGame();
            timer.start();
            overStage.close();
        });

        overRoot.add(buttonsGrid,1,3);
        overStage.setScene(overScene);
        overStage.setResizable(false);
        overStage.setTitle("Game Over!");
        overStage.show();



    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Tetris");
        stage.setResizable(false);
        stage.setScene(this.scene);

        drawGrid();
        playTetris();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

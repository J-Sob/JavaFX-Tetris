/*
        Project title: Tetris
        Author: Jakub Sobczyński
        GitHub: https://github.com/J-Sob
 */


package com.company.tetris;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Engine {       // engine class containing game logic

    enum Shape{         // enumerator used in creating pieces
        ISHAPE,
        TSHAPE,
        RECTSHAPE,
        LSHAPE,
        JSHAPE,
        SSHAPE,
        ZSHAPE,
    }
    enum Direction{     // enumerator used in moving pieces
        RIGHT,
        LEFT,
        DOWN,
    }

    protected class Tetromino{      // inner class to create pieces called "Tetrominos"

        Shape shape;
        int rowPos;         // row and colums positions of the array containing the piece
        int columnPos;
        boolean[][] tetroPosition;

        public Tetromino(){     // constructor that creates piece with random shape on top of the board
            this.tetroPosition = new boolean[4][4];
            for(boolean[] row : tetroPosition) {
                Arrays.fill(row, false);
            }
            this.rowPos = 0;
            this.columnPos = getBoardColumns() / 2 - 2;
            List<Shape> shapes = Arrays.asList(Shape.values());
            Random random = new Random();
            this.shape = shapes.get(random.nextInt(shapes.size()));

            switch (shape) {
                case ISHAPE -> {
                    tetroPosition[1][0] = true;
                    tetroPosition[1][1] = true;
                    tetroPosition[1][2] = true;
                    tetroPosition[1][3] = true;
                }
                case TSHAPE -> {
                    tetroPosition[0][1] = true;
                    tetroPosition[0][2] = true;
                    tetroPosition[0][3] = true;
                    tetroPosition[1][2] = true;
                }
                case RECTSHAPE -> {
                    tetroPosition[0][1] = true;
                    tetroPosition[0][2] = true;
                    tetroPosition[1][1] = true;
                    tetroPosition[1][2] = true;
                }
                case LSHAPE -> {
                    tetroPosition[1][1] = true;
                    tetroPosition[1][2] = true;
                    tetroPosition[0][3] = true;
                    tetroPosition[1][3] = true;
                }
                case JSHAPE -> {
                    tetroPosition[1][1] = true;
                    tetroPosition[1][2] = true;
                    tetroPosition[1][3] = true;
                    tetroPosition[0][1] = true;
                }
                case SSHAPE -> {
                    tetroPosition[0][2] = true;
                    tetroPosition[0][3] = true;
                    tetroPosition[1][1] = true;
                    tetroPosition[1][2] = true;
                }
                case ZSHAPE -> {
                    tetroPosition[0][1] = true;
                    tetroPosition[0][2] = true;
                    tetroPosition[1][2] = true;
                    tetroPosition[1][3] = true;
                }
            }
        }

        public Tetromino(Tetromino tetro){      // copy constructor
            this.shape = tetro.shape;
            this.rowPos = tetro.rowPos;
            this.columnPos = tetro.columnPos;
            tetroPosition = new boolean[4][4];
            for(int i = 0; i < 4; i++){
                tetroPosition[i] = Arrays.copyOf(tetro.tetroPosition[i], tetro.tetroPosition.length);
            }
        }

        public void setTetroPosition(int i, int j, boolean bool){
            this.tetroPosition[i][j] = bool;
        }

        public boolean getTetroPosition(int i, int j){
            return this.tetroPosition[i][j];
        }

        public Shape getShape() {
            return shape;
        }

        public void setShape(Shape shape) {
            this.shape = shape;
        }

        public int getRowPos() {
            return rowPos;
        }

        public void setRowPos(int rowPos) {
            this.rowPos = rowPos;
        }

        public int getColumnPos() {
            return columnPos;
        }

        public void setColumnPos(int columnPos) {
            this.columnPos = columnPos;
        }
    }

    final int boardRows = 20;
    final int boardColumns = 10;
    final boolean[][] gameBoard;
    int score;
    boolean gameOver;
    Tetromino tetro;
    Tetromino upcomingTetro;


    public Engine() {
        gameBoard = new boolean[boardRows][boardColumns];       // initializing board
        this.score = 0;
        this.gameOver = false;
        tetro = new Tetromino();        // currently falling piece
        upcomingTetro = new Tetromino();        // piece next in the line
        clearBoard();
    }

    void checkBoard(){      // function checks if any row has been filled with pieces
        for(int i = boardRows - 1; i >= 0; i--)
        {
            int usedSlots = 0;
            for(int j = 0; j < boardColumns; j++)
            {
                if(gameBoard[i][j]) usedSlots++;
            }
            if(usedSlots == boardColumns)
            {
                clearRow(i);
                addPoints();
                lowerRows(i);
            }
        }
    }

    void moveTetromino(Direction dir){      // moving a piece, if piece collides with either wall or stack, move gets reversed
        switch (dir){
            case DOWN -> {
                if(!gameOver){
                    lowerTetromino();
                }
            }
            case LEFT -> {
                int initialColumnPos = tetro.getColumnPos();
                tetro.setColumnPos(initialColumnPos - 1);
                if(leftCollision() || stackCollision()) {
                    tetro.setColumnPos(initialColumnPos);
                }
            }
            case RIGHT -> {
                int initialColumnPos = tetro.getColumnPos();
                tetro.setColumnPos(initialColumnPos + 1);
                if(rightCollision() || stackCollision()) {
                    tetro.setColumnPos(initialColumnPos);
                }
            }
        }
    }

    void rotateTetromino(){         // same logic as with moving, if new position collides, move gets reversed
        boolean[][] temp = new boolean[4][4];
        for(int i = 0; i < 4; i++)
        {
            for(int j = 0; j < 4; j++)
            {
                temp[i][j] = tetro.getTetroPosition(i,j);
            }
        }

        int initialColumnPos = tetro.getColumnPos();
        for(int i = 0; i < 4; i++)
        {
            for(int j = 0; j < 4; j++)
            {
                tetro.setTetroPosition(i, j, temp[3 - j][i]);
            }
        }

        while(leftCollision())
        {
            tetro.setColumnPos(initialColumnPos + 1);
        }

        while(rightCollision())
        {
            tetro.setColumnPos(initialColumnPos - 1);
        }

        if(stackCollision())
        {
            for(int i = 0; i < 4; i++)
            {
                for(int j = 0; j < 4; j++)
                {
                    tetro.setTetroPosition(i,j,temp[i][j]);
                }
            }
            tetro.setColumnPos(initialColumnPos);
        }
    }

    void lowerTetromino(){      // appending piece, if it collides with either stack or bottom it's added to the stack
        int initialRowPos = tetro.getRowPos();
        tetro.setRowPos(initialRowPos + 1);
        if(bottomCollision() || stackCollision()){
            tetro.setRowPos(initialRowPos);
            addToStack();
            tetro = new Tetromino(upcomingTetro);       // current piece becomes next piece in the line
            upcomingTetro = new Tetromino();        // next piece is generated
            if(stackCollision()){           // if newly created piece collides with stack, game is over
                this.gameOver = true;
            }
        }
    }

    boolean stackCollision(){       // checking for stack collision
        for(int i = 0; i < 4; i++)
        {
            for(int j = 0; j < 4; j++)
            {
                if(tetro.getTetroPosition(i,j))
                {
                    int realColumnPos = tetro.getColumnPos() + j;
                    int realRowPos = tetro.getRowPos() + i;
                    if(gameBoard[realRowPos][realColumnPos])
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    boolean bottomCollision(){      // checking for bottom collision
        for(int i = 0; i < 4; i++)
        {
            for(int j = 0; j < 4; j++)
            {
                if(tetro.getTetroPosition(i, j))
                {
                    int realRowPos = tetro.getRowPos() + i;
                    if(realRowPos == boardRows)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    boolean leftCollision(){        // checking for left wall collision
        for(int i = 0; i < 4; i++)
        {
            for(int j = 0; j < 4; j++)
            {
                if(tetro.getTetroPosition(i, j))
                {
                    int realColumnPos = tetro.getColumnPos() + j;
                    if(realColumnPos < 0)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    boolean rightCollision(){       // checking for right wall collision
        for(int i = 0; i < 4; i++)
        {
            for(int j = 0; j < 4; j++)
            {
                if(tetro.getTetroPosition(i, j))
                {
                    int realColumnPos = tetro.getColumnPos() + j;
                    if(realColumnPos >= boardColumns)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    void addToStack(){      // function changes states in the gameBoard array according to fallen piece position
        for(int i = 0; i < 4; i++)
        {
            for(int j = 0; j < 4; j++)
            {
                if(tetro.getTetroPosition(i, j))
                {
                    int realColumnPos = tetro.getColumnPos() + j;
                    int realRowPos = tetro.getRowPos() + i;
                    gameBoard[realRowPos][realColumnPos] = true;
                }
            }
        }
    }

    void lowerRows(int index){      // lowers all rows before given index
        for(int i = index - 1; i > 0; i--)
        {
            for(int j = 0; j < boardColumns; j++)
            {
                gameBoard[i+1][j] = gameBoard[i][j];
                gameBoard[i][j] = gameBoard[i-1][j];
            }
        }
    }

    void clearBoard(){      // filling array with 0's
        for(boolean[] row : gameBoard){
            Arrays.fill(row,false);
        }
    }

    void clearRow(int index){       // filling row of given index with 0's
        for(int i = 0; i < boardColumns; i++){
            gameBoard[index][i] = false;
        }
    }

    void saveScore(String nickname) throws IOException {        // function saves score after losing with given nickname
        File file = new File("scores.txt");
        if(!file.exists()){
            file.createNewFile();
        }
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file,true));
        bufferedWriter.write("Nickname: " + nickname + ", Score: " + getScore() + "\n");
        bufferedWriter.close();
    }

    void addPoints(){       // very simple points system TODO: multiplier
        this.score += 100;
    }

    void restartGame(){     // preparing for new game
        clearBoard();
        this.score = 0;
        this.gameOver = false;
        this.tetro = new Tetromino();
        this.upcomingTetro = new Tetromino();
    }

    public int getBoardRows() {
        return boardRows;
    }

    public int getBoardColumns() {
        return boardColumns;
    }

    public boolean getTileStatus(int i, int j){
        return this.gameBoard[i][j];
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isGameOver() {
        return gameOver;
    }
}

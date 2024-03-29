package com.codingame.game;

import com.codingame.gameengine.core.SoloGameManager;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Sprite;
import com.codingame.gameengine.module.entities.Text;

import java.util.ArrayList;

public class GraphicHandler {

    public static final int BOARD_HEIGHT = 8;
    public static final int SCREEN_WIDTH = 1920;
    public static final int SCREEN_HEIGHT = 1080;

    private static double getOffset(){
        return 1.25+100+1.25+13.5;
    }

    private static int getBaseX(Player player){
        return (int)(SCREEN_WIDTH/2) - (int)getOffset()*(Constant.WORD_LEN/2);
    }

    private static int getBaseY(Player player){
        return 50;

        //for length 6
//        return (int)SCREEN_HEIGHT/2 - 280;
    }

    public static void drawLetter(Player player, char letter, int state, int row, int column, GraphicEntityModule graphicEntityModule){
        double word_type_time = 0.125;
        double word_result_time = 1-word_type_time;

        if(row == player.outputHistory.size()-1){
            player.board.get(row)[column].setImage(String.format("letters/%c0.png", Character.toLowerCase(letter)));
            graphicEntityModule.commitEntityState((word_type_time/Constant.WORD_LEN)*(column+1), player.board.get(row)[column]);

            player.board.get(row)[column].setImage(String.format("letters/%c%d.png", Character.toLowerCase(letter), state));
            graphicEntityModule.commitEntityState(word_type_time+(word_result_time/Constant.WORD_LEN)*(column+1), player.board.get(row)[column]);
        }
        else{
            player.board.get(row)[column].setImage(String.format("letters/%c%d.png", Character.toLowerCase(letter), state));
        }

    }

    public static void drawBackgroundImage(GraphicEntityModule graphicEntityModule){
        graphicEntityModule.createSprite()
                .setImage("Background.jpg")
                .setAnchor(0)
                .setX(0)
                .setY(0);
    }

    public static void drawLiveBoard(Player player, GraphicEntityModule graphicEntityModule){
        for(int i=0; i<player.outputHistory.size(); i++){
            LetterState[] output = player.outputHistory.get(i);
            for(int j=0; j<Constant.WORD_LEN; j++){
                LetterState letterState = output[j];
                drawLetter(player, letterState.getLetter(), letterState.getState(), i, j, graphicEntityModule);
            }
        }
    }

    public static void drawGrids(SoloGameManager<Player> gameManager, GraphicEntityModule graphicEntityModule) {
        Player player = gameManager.getPlayer();
        int base_x = getBaseX(player);
        int base_y = getBaseY(player);

        player.board = new ArrayList<Sprite[]>();

        for(int i=0; i<BOARD_HEIGHT; i++){
            Sprite[] rw = new Sprite[Constant.WORD_LEN];

            for(int j=0; j<Constant.WORD_LEN; j++){
                rw[j] = graphicEntityModule.createSprite()
                        .setImage("empty_cell.png")
                        .setScale(0.5)
                        .setAnchor(0)
                        .setX((int) (base_x + j*getOffset()))
                        .setY((int) (base_y + i*getOffset()));
            }

            player.board.add(rw);

        }
    }


    public static void drawHud(SoloGameManager<Player> gameManager, GraphicEntityModule graphicEntityModule) {
        graphicEntityModule.createSprite()
                .setImage(gameManager.getPlayer().getAvatarToken())
                .setX(SCREEN_WIDTH-10)
                .setY(SCREEN_HEIGHT-10)
                .setAnchorX(1)
                .setAnchorY(1)
                .setZIndex(0)
                .setAlpha(0.5)
                .setBaseHeight(100)
                .setBaseWidth(100)
        ;
    }
}

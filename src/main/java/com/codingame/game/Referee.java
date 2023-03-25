package com.codingame.game;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.SoloGameManager;
import com.codingame.gameengine.module.endscreen.EndScreenModule;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.google.inject.Inject;

/*
* ideas to work with:
* 1. duplicate letter in word
* 2. if a letter is unlocked for a position remove the position and continue the turn with the new word
*
*/

public class Referee extends AbstractReferee {
    // Uncomment the line below and comment the line under it to create a Solo Game
     @Inject private SoloGameManager<Player> gameManager;
    @Inject private GraphicEntityModule graphicEntityModule;
    @Inject private EndScreenModule endScreenModule;




    private String magicalWord; //the word needs to guess
    private int guessNeeded = 0;
    private boolean isAWin = false;
    private boolean isInvalidAction = false;

    //process output and update player's states
    private void process_output(Player player, String output){
        if (output.length() != Constant.WORD_LEN){
            isInvalidAction = true;
            gameManager.loseGame("Invalid guess, try again.");
            return;
        }

        String[] report = new String[Constant.WORD_LEN];
        LetterState[] letterStates = new LetterState[Constant.WORD_LEN];
        //preparing player's next turninput state and generating report for current turn
        for(int i=0; i<Constant.WORD_LEN; i++){
            char ch = output.charAt(i);
            if(!(ch>='a' && ch<='z') && !(ch>='A' && ch<='Z')){
                isInvalidAction = true;
                gameManager.loseGame("Invalid guess, try again.");
                return;
            }

            int state, pos;
            if(magicalWord.charAt(i) == ch){
                state = Constant.ALL_KNOWN_STATE;
                pos = i+1;
            }
            else if(magicalWord.indexOf(ch) != -1){
                state = Constant.POSITION_UNKNOWN_STATE;
                pos = -(i+1);
            }
            else{
                state = Constant.ABSENT_STATE;
                pos = Constant.ABSENT_POSITION;
            }

            player.updateTurnInput(i, new LetterState(ch, state, pos));
            report[i] = String.format("(%c, %d)", ch, state);

            letterStates[i] = new LetterState(ch, state, pos);

        }
        gameManager.addToGameSummary(
            String.format("Guess: %s - %s", output, String.join(" ", report))
        );

        player.outputHistory.add(letterStates);
        if(player.outputHistory.size() > GraphicHandler.BOARD_HEIGHT){
            player.outputHistory.remove(0);
        }

        //updating player alphabet state
        //aren't updating a letters state if previously reviled it's better state
        for(int i=0; i<Constant.WORD_LEN; i++){
            char ch = output.charAt(i);
            int ch_prev_state = player.getAlphabetStates().get(ch).getState();

            if (magicalWord.charAt(i) == ch){
                player.updateAlphabetStates(ch, Constant.ALL_KNOWN_STATE, i+1);
            }
            else if(magicalWord.indexOf(ch)!=-1 && ch_prev_state<=Constant.POSITION_UNKNOWN_STATE){
                player.updateAlphabetStates(ch, Constant.POSITION_UNKNOWN_STATE, -(i+1));
            }
            else if(ch_prev_state==Constant.UNKNOWN_STATE) {
                player.updateAlphabetStates(ch, Constant.ABSENT_STATE, Constant.ABSENT_POSITION);
            }
        }

    }

    private List<String> getWordSet(){
        List<String> wordSet = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(Constant.WORDS_FILE_PATH)));
            for(int i=0; i<Constant.WORD_COUNT; i++){
                wordSet.add(reader.readLine());
            }
            Collections.shuffle(wordSet);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return  wordSet;
    }

    private void sendInitialInput(){
        gameManager.getPlayer().sendInputLine(String.valueOf(magicalWord.length()));

    }

    private void setMagicalWord(){
        String[] inputs = gameManager.getTestCaseInput().get(0).split("\\s+");
        if(inputs[0].equals("T")){
            magicalWord = inputs[1];
        }
        else {
//            magicalWord = inputs[new Random().nextInt(inputs.length-2) + 2];
            int length = Integer.parseInt(inputs[1]);
            magicalWord = new Random().ints(length, 'A', 'Z' + 1).mapToObj(c -> String.valueOf((char) c)).collect(Collectors.joining());
        }
//        System.err.println("magial word " + magicalWord);

//        System.err.println("TesCase Input " + String.join(" ", inputs));
//        System.out.println(gameManager.getTestCaseInput().toString());
        Constant.WORD_LEN = magicalWord.length();

//        System.out.println(String.format("%s %d", magicalWord, magicalWord.length()));
    }

    @Override
    public void init() {
        // Initialize your game here.
        gameManager.setFrameDuration(1300);
        gameManager.setMaxTurns(26);
        gameManager.setFirstTurnMaxTime(1000);
        gameManager.setTurnMaxTime(50);

//        System.out.println("Game properties:");
//        System.out.println(String.format("FirstTurnMaxTime: %d\n TurnMaxTime: %d\n MaxTurns: %d", gameManager.getFirstTurnMaxTime(), gameManager.getTurnMaxTime(), gameManager.getMaxTurns()));

        setMagicalWord();
        sendInitialInput();

        GraphicHandler.drawBackgroundImage(graphicEntityModule);
        GraphicHandler.drawGrids(gameManager, graphicEntityModule);
        GraphicHandler.drawHud(gameManager, graphicEntityModule);

    }

    @Override
    public void gameTurn(int turn) {
        guessNeeded = turn;
        //sending input and execute
        Player player = gameManager.getPlayer();
        player.sendInputLine(player.generateInputLine());
        player.execute();


        //getting outputs and process
        try {
            List<String> outputs = player.getOutputs();
            String output = outputs.get(0).toUpperCase();

            process_output(player, output);
            GraphicHandler.drawLiveBoard(player, graphicEntityModule);

            if(magicalWord.equals(output)){
                isAWin = true;
                gameManager.winGame("Congratulations! You guessed it right.");
            }
        } catch (TimeoutException e) {
            gameManager.loseGame("Timeout!");
        }
    }


    @Override
    public void onEnd() {
        if(!isAWin && !isInvalidAction){
            gameManager.addToGameSummary("You're getting close, try again soon.");
        }
        gameManager.putMetadata("guess", String.valueOf(guessNeeded));

        endScreenModule.setScores( new int[]{}, new String[]{isAWin ? "Success!" : "Try again"});
    }



}

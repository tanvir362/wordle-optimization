package com.codingame.game;
import com.codingame.gameengine.core.AbstractSoloPlayer;
import com.codingame.gameengine.module.entities.Group;
import com.codingame.gameengine.module.entities.Sprite;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.CheckedOutputStream;

// Uncomment the line below and comment the line under it to create a Solo Game
 public class Player extends AbstractSoloPlayer {
    private HashMap<Character, LetterState> alphabetStates;
    private List<LetterState> turnInput;

    public Sprite hud;
    public List<Sprite[]> board;
    public List<LetterState[]> outputHistory;

    Player(){
        super();

        alphabetStates = new HashMap<>();

        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for(Character ch: alphabet.toCharArray()){
            alphabetStates.put(ch, new LetterState(ch, Constant.UNKNOWN_STATE, Constant.UNKNOWN_POSITION));
        }

        turnInput = new ArrayList<LetterState>();
        outputHistory = new ArrayList<LetterState[]>();
    }

    @Override
    public int getExpectedOutputLines() {
        // Returns the number of expected lines of outputs for a player

        // TODO: Replace the returned value with a valid number. Most of the time the value is 1. 
        return 1;
    }

    public void setWInner(boolean isWInner){
    }

    public void updateAlphabetStates(Character ch, int state, int pos){
        this.alphabetStates.replace(ch, new LetterState(ch, state, pos));
    }

    public  HashMap<Character, LetterState> getAlphabetStates(){
        return this.alphabetStates;
    }

    public String generateInputLine(){
        if(turnInput.isEmpty()){
            for(int i=0; i<Constant.WORD_LEN; i++){
                turnInput.add(new LetterState(Constant.UNKNOWN_LETTER, Constant.UNKNOWN_STATE, Constant.UNKNOWN_POSITION));
            }
        }

        String line = "";
        for(int i=0; i<Constant.WORD_LEN; i++){
            line += String.format("%d ", turnInput.get(i).getState());
        }

        return line;
    }

    public void updateTurnInput(int index, LetterState state) {
        turnInput.set(index, state);
    }

}

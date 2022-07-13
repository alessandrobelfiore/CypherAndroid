package com.example.cypher00;

class KeysUtils {

    // KEYS
    static final String MODE_KEY = "MODE_KEY";
    static final String LEVEL_ID_KEY = "LEVEL_ID_KEY";
    static final String COVERS_KEY = "COVERS_KEY";
    static final String MESSAGE_CODE = "MESSAGE_CODE";
    static final String DIFFICULTY_KEY = "DIFFICULTY_KEY";
    static final String PITCH_KEY = "PITCH_KEY";
    static final String ROLL_KEY = "ROLL_KEY";
    static final String GRID_BYTES_KEY = "GRID_BYTES_KEY";
    static final String MY_MATCH_TIME = "MY_MATCH_TIME";
    static final String WINNER_MATCH_TIME = "WINNER_MATCH_TIME";
    static final String WINNER_KEY = "WINNER_KEY";
    static final String OPPONENT_KEY = "OPPONENT_KEY";
    static final String SYNCHRONY_KEY = "set_synchrony";
    static final String ANIM_KEY = "set_animation_time";
    static final String PLAY_AGAIN_KEY = "play_again_key";
    static final String HOST_KEY = "HOST_KEY";
    static final String MY_DEVICE_NAME_KEY ="MY_DEVICE_NAME_KEY";


    static final int MESSAGE_READ = 0;

    // MESSAGE CODES
    static final byte READY = 1;
    static final byte NOTIFY = 2;
    static final byte RESULT = 3;
    static final byte PASS_GRID = 4;
    static final byte PLAY_AGAIN = 5;
    static final byte INSERT_DB = 6;
    static final byte GET_MATCHES = 7;

    // DIFFICULTIES
    static final int DIFFICULTY_EASY = 10;
    static final int DIFFICULTY_MEDIUM = 11;
    static final int DIFFICULTY_HARD = 12;

    // COVERS NUMBER
    static final int COVERS_NO = 20;
    static final int COVERS_SOME = 21;
    static final int COVERS_YES = 22;

    // GAME MODES
    static final int SINGLE_PLAYER_TRAINING = 31;
    static final int SINGLE_PLAYER_CAMPAIGN = 32;
    static final int MULTI_PLAYER_CLIENT = 33;
    static final int MULTI_PLAYER_HOST = 34;
}

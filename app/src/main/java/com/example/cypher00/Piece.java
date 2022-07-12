package com.example.cypher00;

class Piece {
    private int bitmask;
    private int N_MASK = 0b1000;
    private int E_MASK = 0b0100;
    private int S_MASK = 0b0010;
    private int W_MASK = 0b0001;

    Piece(int init) {
        this.bitmask = init;
    }

    /**
     * Rotates logically the piece, updating its bitmask
     */
    void rotate() {
        // if the rightmost bit is set, we preserve it and put it in the first position
        this.bitmask = this.bitmask >> 1 | ((this.bitmask & W_MASK) << 3);
    }

    /**
     * Rotates logically the piece n times, updating its bitmask
     */
    void rotate(int n) {
        // for (int i = 0; i < n; i++) rotate();
        n = n % 4;
        this.bitmask = (this.bitmask | ((this.bitmask & ~(~0b0 << n)) << 4)) >> n;
    }

    int getBitmask() {
        return this.bitmask;
    }

    byte getBitmaskAsByte() {
        return (byte) this.bitmask;
    }

    //

    /**
     * Checks if a direction bit is set
     * @param direction the direction given
     * @return true if the given direction is set
     *         false otherwise
     */
    boolean has(Direction direction) {
        switch (direction) {
            case NORTH:
                return (bitmask & N_MASK) == N_MASK;
            case EAST:
                return (bitmask & E_MASK) == E_MASK;
            case SOUTH:
                return (bitmask & S_MASK) == S_MASK;
            case WEST:
                return (bitmask & W_MASK) == W_MASK;
            default: return false;
        }
    }

    /**
     * Removes a direction stick from the bitmask
     * @param direction the direction given
     */
    void remove(Direction direction) {
        switch (direction) {
            case NORTH:
                this.bitmask = bitmask - N_MASK;
                break;
            case EAST:
                this.bitmask = bitmask - E_MASK;
                break;
            case SOUTH:
                this.bitmask = bitmask - S_MASK;
                break;
            case WEST:
                this.bitmask = bitmask - W_MASK;
                break;
            default: break;
        }
    }

    enum Direction {
        NORTH, EAST, SOUTH, WEST
    }

}

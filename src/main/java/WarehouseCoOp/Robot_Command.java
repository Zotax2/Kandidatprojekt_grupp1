package WarehouseCoOp;

public enum Robot_Command {
    MOVE("a4"), BACK("b"), TURN_RIGHT("c"), TURN_LEFT("d"), STOP("e"), START_N("h"), START_S("i"), START_E("j"), START_W("k"), PICK("l"), STATUS("s");
    private final String COMMAND_CHAR;

    Robot_Command(String command) {
        this.COMMAND_CHAR = command;

    }

    public String getCOMMAND_CHAR() {
        return COMMAND_CHAR;
    }
}

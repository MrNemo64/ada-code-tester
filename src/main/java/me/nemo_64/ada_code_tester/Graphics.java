package me.nemo_64.ada_code_tester;

public class Graphics {

    public static final String RED_COLOR = "\u001b[31m";
    public static final String GREEN_COLOR = "\u001b[32m";
    public static final String RESET = "\u001b[0m";
    public static final String CLEAR_SCREEN = "\u001b[2J";
    public static final String GOTO_COORD = "\u001b[%d;%dH";
    public static final String SHOW_CURSOR = "\u001b[?25h";
    public static final String HIDE_CURSOR = "\u001b[?25l";

    public static void cls() {
        System.out.println(CLEAR_SCREEN);
    }

    public static void gotoXY(int x, int y) {
        System.out.print(String.format(GOTO_COORD, x, y));
    }

    public static void showCursor() {
        System.out.print(SHOW_CURSOR);
    }

    public static void hideCursor() {
        System.out.print(HIDE_CURSOR);
    }

    public static void clearLine() {
        System.out.print("\u001b[2K");
    }
}

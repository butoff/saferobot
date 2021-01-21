import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Game {

    final private List<List<Cell>> cells = new ArrayList<>();
    final private List<Cell> bases = new ArrayList<>();
    final int width;
    final int height;

    public static void main(String[] args) {
        Game g = new Game(System.in);
        g.explore();
        System.out.println(g);
    }

    Game(InputStream is) {
        Scanner sc = new Scanner(is);
        int h = 0, w = 0;
        while (sc.hasNextLine()) {
            String s = sc.nextLine();
            if (w == 0)
                w = s.length();
            else if (s.length() != w)
                throw new IllegalArgumentException("wrong width: " + s.length() + ", must be " + w);
            cells.add(listFrom(s));
            h++;
        }
        width = w;
        height = h;

        for (h = 0; h < height; h++)
            for (w = 0; w < width; w++) {
                Cell c = at(h, w);
                c.w = w;
                c.h = h;
                if (c.type == Type.BASE)
                    bases.add(c);
                if (c.type == Type.WALL) {
                    if (h > 0) at(h-1, w).setS(Direction.WALL);
                    if (h < height-1) at(h+1, w).setN(Direction.WALL);
                    if (w > 0) at(h, w-1).setO(Direction.WALL);
                    if (w < width-1) at(h, w+1).setW(Direction.WALL);
                }
            }
    }

    private Cell at(int h, int w) {
        return cells.get(h).get(w);
    }

    // find safe fields and change their type to SAFE
    void explore() {
        for (Cell b : bases) {
            for (int d = 0; d < 4; d++) {
                visit(b, d);
            }
        }
    }

    Cell at(Cell c, int direction) {
        int h = c.h;
        int w = c.w;
        switch (direction) {
            case 0: return at(h-1, w);
            case 1: return at(h, w-1);
            case 2: return at(h+1, w);
            case 3: return at(h, w+1);
            default: throw new IllegalStateException("wtf, direction = " + direction);
        }
    }

    void visit(Cell current, int direction) {
        Cell f = at(current, direction);
        Cell l = at(current, left(direction));
        Cell r = at(current, right(direction));

        if (current.directions[direction] == Direction.LEADS_TO_BASE || current.directions[direction] == Direction.WALL) {
            if (l.type == Type.WALL && r.type == Type.FREE) {
                r.markDirectionAsLeadingToBase(left(direction));
                visit(r, right(direction));
            }
            if (r.type == Type.WALL && l.type == Type.FREE) {
                l.markDirectionAsLeadingToBase(right(direction));
                visit(l, left(direction));
            }
        }
        if (f.type == Type.BASE)
            return;
        if (f.type != Type.WALL) {
            f.markDirectionAsLeadingToBase(opposite(direction));
            visit(f, direction);
        } else if (current.wallCount() == 3) {
            direction = opposite(direction);
            visit(current, direction);
        } else if (!current.hasUnexploredDirections() && current.type != Type.BASE) {
            // all directions are explored and leads to base
            current.type = Type.SAFE;
        }
    }

    static int opposite(int direction) {
        return (direction + 2) % 4;
    }

    static int left(int direction) {
        return (direction + 3) % 4;
    }

    static int right(int direction) {
        return (direction + 1) % 4;
    }

    List<Cell> listFrom(String s) {
        final List<Cell> l = new ArrayList<>();
        for (char ch : s.toCharArray())
            l.add(new Cell(ch));
        return l;
    }

    class Cell {
        private int w;
        private int h;
        private Type type;
        private final Direction[] directions = new Direction[4];

        Cell(char ch) {
            type = Type.from(ch);
            for (int i = 0; i < 4; i++)
                directions[i] = Direction.UNEXPLORED;
        }

        void setN(Direction d) { directions[0] = d; }
        void setW(Direction d) { directions[1] = d; }
        void setS(Direction d) { directions[2] = d; }
        void setO(Direction d) { directions[3] = d; }

        boolean hasUnexploredDirections() {
            for (int i = 0; i < 4; i++)
                if (directions[i] == Direction.UNEXPLORED)
                    return true;
            return false;
        }

        int wallCount() {
            int wc = 0;
            for (int i = 0; i < 4; i++)
                if (directions[i] == Direction.WALL)
                    wc++;
            return wc;
        }

        void markDirectionAsLeadingToBase(int direction) {
            directions[direction] = Direction.LEADS_TO_BASE;
            if (type != Type.BASE && !hasUnexploredDirections())
                type = Type.SAFE;
        }

        @Override
        public String toString() {
            switch (type) {
                case FREE: return " ";
                case WALL: return "#";
                case BASE: return "B";
                case SAFE: return "S";
            }
            throw new IllegalStateException("unknown type: " + type);
        }
    }

    // type of the cell
    enum Type {
        WALL('#'), FREE(' '), BASE('B'), SAFE('S');

        private char asChar;

        Type(char asChar) {
            this.asChar = asChar;
        }

        static Type from(char ch) {
            for (Type t : values())
                if (t.asChar == ch)
                    return t;
            throw new IllegalArgumentException("wrong char: " + ch);
        }

        @Override
        public String toString() {
            return Character.toString(asChar);
        }
    };

    // one of four directions for each cell
    enum Direction {
        WALL,  // next cell is wall
        UNEXPLORED,  // not explored yet
        LEADS_TO_BASE  // will eventially lead to base
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (List<Cell> row : cells) {
            for (Cell c : row)
                sb.append(c);
            sb.append("\n");
        }
        return sb.toString();
    }
}

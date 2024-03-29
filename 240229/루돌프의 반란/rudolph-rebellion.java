import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Main {
    private static final int DOLPH = 100;
    private static final int[][] santa = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};
    private static final int[][] dolph = {{1, 1}, {1, 0}, {1, -1}, {0, 1}, {0, -1}, {-1, 1}, {-1, 0}, {-1, -1}};
    private static int N, M, P, C, D;
    private static int[][] squares;
    private static Map<Integer, Santa> santas;

    // TODO: 루돌프 번호 바꾸기!
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        N = Integer.parseInt(st.nextToken());
        M = Integer.parseInt(st.nextToken());
        P = Integer.parseInt(st.nextToken());
        C = Integer.parseInt(st.nextToken());
        D = Integer.parseInt(st.nextToken());
        squares = new int[N][N];
        st = new StringTokenizer(br.readLine());
        int dolphX = Integer.parseInt(st.nextToken()) - 1;
        int dolphY = Integer.parseInt(st.nextToken()) - 1;
        santas = new HashMap<>();
        squares[dolphX][dolphY] = DOLPH;
        for (int i = 0; i < P; i++) {
            st = new StringTokenizer(br.readLine());
            int num = Integer.parseInt(st.nextToken());
            int x = Integer.parseInt(st.nextToken()) - 1;
            int y = Integer.parseInt(st.nextToken()) - 1;
            squares[x][y] = num;
            santas.put(num, new Santa(num, x, y, 0, 0, false, -1));
        }

        for (int turn = 1; turn <= M; turn++) {
            boolean isBreak = true;
            for (Santa s : santas.values()) {
                isBreak = isBreak && s.isOut;
            }
            if (isBreak) {
                break;
            }

//            System.out.println(dolphX + " : " + dolphY);
            int[] dolphRush = moveDolph(dolphX, dolphY);

            if (squares[dolphRush[1]][dolphRush[2]] == 0) {
                swap(dolphX, dolphY, dolphRush[1], dolphRush[2]);
            } else {
                Santa hit = santas.get(squares[dolphRush[1]][dolphRush[2]]);
                hit.score += C;

                int newSantaX = hit.x + (dolph[dolphRush[3]][0] * C);
                int newSantaY = hit.y + (dolph[dolphRush[3]][1] * C);

                if (isIn(newSantaX, newSantaY)) {
                    hit.isStun = turn + 2;
                    squares[hit.x][hit.y] = 0;
                    collide(newSantaX, newSantaY, hit.num, dolph[dolphRush[3]]);
                    hit.x = newSantaX;
                    hit.y = newSantaY;
                } else {
                    hit.isOut = true;
                    squares[hit.x][hit.y] = 0;
                }
                swap(dolphX, dolphY, dolphRush[1], dolphRush[2]);
            }
            dolphX = dolphRush[1];
            dolphY = dolphRush[2];

//            System.out.println("====dolph start====");
//            for (int i = 0; i < N; i++) System.out.println(Arrays.toString(squares[i]));
//            System.out.println("=====dolph end====");


            for (Santa s : santas.values()) {
                if (s.isOut) continue;
                if (s.isStun > turn) continue;

                int[] newPosition = moveSanta(s.x, s.y, dolphX, dolphY);
                // 산타가 루돌프를 만난 경우
                if (squares[newPosition[0]][newPosition[1]] == DOLPH) {
                    s.score += D;
                    s.direction = reversePosition(false, newPosition[2]);

                    int newSantaX = newPosition[0] + (santa[s.direction][0] * D);
                    int newSantaY = newPosition[1] + (santa[s.direction][1] * D);
                    if (isIn(newSantaX, newSantaY)) {
                        s.isStun = turn + 2;
                        squares[s.x][s.y] = 0;
                        collide(newSantaX, newSantaY, s.num, santa[s.direction]);
                        s.x = newSantaX;
                        s.y = newSantaY;
                    } else {
                        s.isOut = true;
                        squares[s.x][s.y] = 0;
                    }
                } else {
                    swap(s.x, s.y, newPosition[0], newPosition[1]);
                    s.x = newPosition[0];
                    s.y = newPosition[1];
                }
            }
            for (Santa s : santas.values()) {
                if (s.isOut) continue;
                s.score += 1;
            }
//            for (int i = 0; i < N; i++) System.out.println(Arrays.toString(squares[i]));
//            System.out.println("=========");
//            santas.values().forEach(System.out::println);
//            System.out.println("======turn: " + turn + "over=====");
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= P; i++) {
            builder.append(santas.get(i).score).append(" ");
        }
        System.out.print(builder);
    }

    private static int reversePosition(boolean isDolph, int position) {
        if (isDolph) {
            return 0;
        } else {
            int newPosition = position - 2;
            if (newPosition < 0) {
                newPosition += 4;
            }
            return newPosition;
        }
    }

    private static void collide(int x, int y, int num, int[] delta) {
        if (squares[x][y] == 0) {
            squares[x][y] = num;
            return;
        }

        Santa meet = santas.get(squares[x][y]);

        int newX = x + delta[0];
        int newY = y + delta[1];
        // 나간 경우
        if (!isIn(newX, newY)) {
            meet.isOut = true;
            squares[x][y] = num;
        } else {
            int temp = squares[x][y];
            squares[x][y] = num;
            collide(newX, newY, temp, delta);
            meet.x = newX;
            meet.y = newY;
        }
    }

    private static void swap(int x1, int y1, int x2, int y2) {
        int temp = squares[x1][y1];
        squares[x1][y1] = squares[x2][y2];
        squares[x2][y2] = temp;
    }

    private static int[] moveDolph(int dx, int dy) {
        List<int[]> array = new ArrayList<>();
        for (Santa s : santas.values()) {
            if (s.isOut) continue;
            for (int i = 0; i < dolph.length; i++) {
                int newX = dx + dolph[i][0];
                int newY = dy + dolph[i][1];

                if (isIn(newX, newY)) {
                    int distance = (int) (Math.pow(newX - s.x, 2) + Math.pow(newY - s.y, 2));
                    int newDistance = (int) (Math.pow(dx - s.x, 2) + Math.pow(dy - s.y, 2));
                    array.add(new int[]{distance, newX, newY, i, s.x, s.y, newDistance});
                }
            }
        }
        Collections.sort(array, (p1, p2) -> {
            int compare = Integer.compare(p1[6], p2[6]);
            if (compare == 0) {
                compare = Integer.compare(p1[0], p2[0]);
                if (compare == 0) {
                    compare = Integer.compare(p2[4], p1[4]);
                    if (compare == 0) {
                        compare = Integer.compare(p2[5], p1[5]);
                        return compare;
                    }
                    return compare;
                }
                return compare;
            }
            return compare;
        });
        return array.size() == 0 ? new int[]{0, dx, dy, 0} : array.get(0);
    }

    private static int[] moveSanta(int sx, int sy, int dx, int dy) {
        int min = (int) (Math.pow(sx - dx, 2) + Math.pow(sy - dy, 2));
        int[] position = {sx, sy, 0};
        for (int i = 0; i < santa.length; i++) {
            int newX = sx + santa[i][0];
            int newY = sy + santa[i][1];

            if (isIn(newX, newY) && (squares[newX][newY] == 0 || squares[newX][newY] == DOLPH)) {
                if (squares[newX][newY] == DOLPH) {
                    return new int[]{newX, newY, i};
                }
                int distance = (int) (Math.pow(newX - dx, 2) + Math.pow(newY - dy, 2));
                if (min > distance) {
                    position[0] = newX;
                    position[1] = newY;
                    min = distance;
                }
//                else if (min == distance) {
//                    if (position[0] < newX) {
//                        position[0] = newX;
//                        position[1] = newY;
//                        position[2] = i;
//                    } else if (position[0] == newX) {
//                        if (position[1] < newY) {
//                            position[1] = newY;
//                            position[2] = i;
//                        }
//                    }
//                }
            }
        }
        return position;
    }

    private static boolean isIn(int x, int y) {
        return x >= 0 && y >= 0 && x < N && y < N;
    }

    static class Santa {
        int num;
        int x;
        int y;
        int score;
        int direction;
        boolean isOut;
        int isStun;

        public Santa(int num, int x, int y, int score, int direction, boolean isOut, int isStun) {
            this.num = num;
            this.x = x;
            this.y = y;
            this.score = score;
            this.direction = direction;
            this.isOut = isOut;
            this.isStun = isStun;
        }

        @Override
        public String toString() {
            return "Santa{" +
                    "num=" + num +
                    ", x=" + x +
                    ", y=" + y +
                    ", score=" + score +
                    ", direction=" + direction +
                    ", isOut=" + isOut +
                    ", isStun=" + isStun +
                    '}';
        }
    }
}
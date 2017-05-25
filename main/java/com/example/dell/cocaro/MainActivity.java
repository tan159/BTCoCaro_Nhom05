package com.example.dell.cocaro;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ActionMenuView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity {
    final static int max = 15;
    private Context context;
    private Button btNew;
    private TextView txTurn;
    private int[][] vcell = new int[max][max];
    private int win;
    private boolean fMove;
    private int xSet;
    private int ySet;
    private int turnP;
    private ImageView[][] cell = new ImageView[max][max];
    private Drawable[] dcell = new Drawable[4];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        loadResource();
        setListen();
        createBoard();
    }

    private void setListen() {
        btNew = (Button) findViewById(R.id.btNew);
        txTurn = (TextView) findViewById(R.id.txTurn);
        btNew.setText("Chơi với CPU");
        txTurn.setText("Bấm để chơi");
        btNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                create_game();
                play_game();
            }
        });
    }

    private void play_game() {
        Random r = new Random();
        turnP = r.nextInt(2) + 1;
        if (turnP == 1) {
            Toast.makeText(context, "Bạn đi trước", Toast.LENGTH_LONG).show();
            playerTurn();
        } else {
            Toast.makeText(context, "CPU đi trước", Toast.LENGTH_LONG).show();
            cpuTurn();
        }
    }

    private void cpuTurn() {

        txTurn.setText("CPU [X]");
        if (fMove) {
            fMove = false;
            xSet = 7;
            ySet = 7;
            make_move();
        } else {
            cpuMove();
            make_move();
        }
    }

    private final int[] irow = {-1, -1, -1, 0, 1, 1, 1, 0};
    private final int[] icol = {-1, 0, 1, 1, 1, 0, -1, -1};

    private void cpuMove() {
        List<Integer> lsX = new ArrayList<Integer>();
        List<Integer> lsY = new ArrayList<Integer>();
        final int range = 2;
        for (int i = 0; i < max; i++) {
            for (int j = 0; j < max; j++) {
                if (vcell[i][j] != 0) {
                    for (int t = 1; t <= range; t++) {
                        for (int k = 0; k < 8; k++) {
                            int x = i + irow[k] * t;
                            int y = j + icol[k] * t;
                            if (inB(x, y) && vcell[x][y] == 0) {
                                lsX.add(x);
                                lsY.add(y);
                            }
                        }
                    }
                }
            }
        }
        int lx = lsX.get(0);
        int ly = lsY.get(0);
        int res = Integer.MAX_VALUE - 10;
        for (int i = 0; i < lsX.size(); i++) {
            int x = lsX.get(i);
            int y = lsY.get(i);
            vcell[x][y] = 2;
            int gp = getPos();
            if (gp < res) {
                res = gp;
                lx = x;
                ly = y;
            }
            vcell[x][y] = 0;
        }
        xSet = lx;
        ySet = ly;
    }

    private int getPos() {
        int gp = 0;
        int pl = turnP;
        for (int i = 0; i < max; i++) {
            gp += ckValue(max - 1, i, -1, 0, pl);
        }
        for (int i = 0; i < max; i++) {
            gp += ckValue(i, max - 1, 0, -1, pl);
        }
        for (int i = max - 1; i >= 0; i--) {
            gp += ckValue(i, max - 1, -1, -1, pl);
        }
        for (int i = max - 2; i >= 0; i--) {
            gp += ckValue(max - 1, i, -1, -1, pl);
        }
        for (int i = max - 1; i >= 0; i--) {
            gp += ckValue(i, 0, -1, 1, pl);
        }
        for (int i = max - 1; i >= 1; i--) {
            gp += ckValue(max - 1, i, -1, 1, pl);
        }
        return gp;
    }

    private int ckValue(int xd, int yd, int vx, int vy, int pl) {
        int i;
        int j;
        int gp = 0;
        i = xd;
        j = yd;
        String str = String.valueOf(vcell[i][j]);
        while (true) {
            i += vx;
            j += vy;
            if (inB(i, j)) {
                str = str + String.valueOf(vcell[i][j]);
                if (str.length() == 6) {
                    gp += Evaluation(str, pl);
                    str = str.substring(1, 6);
                }
            } else {
                break;
            }
        }
        return gp;
    }

    private void playerTurn() {

        txTurn.setText("người chơi [O]");
        fMove = false;
        ckclicked = false;
    }

    private void make_move() {

        cell[xSet][ySet].setImageDrawable(dcell[turnP]);
        vcell[xSet][ySet] = turnP;
        if (ckEmptyCell()) {
            Toast.makeText(context, "Hòa", Toast.LENGTH_SHORT).show();
            return;
        } else {
            if (ckWin()) {
                if (win == 1) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Chúc mừng")
                            .setMessage("Bạn đã thắng")
                            .setPositiveButton("Chơi lại", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    create_game();
                                    play_game();
                                }
                            })
                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    txTurn.setText("Bạn đã thắng");
                                }
                            })
                            .setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                    builder.show();

                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Rất tiếc")
                            .setMessage("Bạn đã thua")
                            .setPositiveButton("Chơi lại", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    create_game();
                                    play_game();
                                }
                            })
                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    txTurn.setText("CPU thắng");
                                }
                            })
                            .setNegativeButton("Thoát", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            });
                    builder.show();
                }
                return;
            }
        }
        if (turnP == 1) {
            turnP = (1 + 2) - turnP;
            cpuTurn();
        } else {
            turnP = 3 - turnP;
            playerTurn();
        }
    }

    private boolean ckEmptyCell() {
        for (int i = 0; i < max; i++) {
            for (int j = 0; j < max; j++) {
                if (vcell[i][j] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean ckWin() {
        if (win != 0) {
            return true;
        }
        vEnd(xSet, 0, 0, 1, xSet, ySet);
        vEnd(0, ySet, 1, 0, xSet, ySet);
        if (xSet + ySet >= max - 1) {
            vEnd(max - 1, xSet + ySet - max + 1, -1, 1, xSet, ySet);
        } else {
            vEnd(xSet + ySet, 0, -1, 1, xSet, ySet);
        }
        if (xSet <= ySet) {
            vEnd(xSet - ySet + max - 1, max - 1, -1, -1, xSet, ySet);
        } else {
            vEnd(max - 1, max - 1 - (xSet - ySet), -1, -1, xSet, ySet);
        }
        if (win != 0) {
            return true;
        } else {
            return false;
        }
    }

    private void vEnd(int xx, int yy, int vx, int vy, int rx, int ry) {
        if (win != 0) {
            return;
        }
        final int range = 4;
        int i;
        int j;
        int xb = rx - range * vx;
        int yb = ry - range * vy;
        int xa = rx + range * vx;
        int ya = ry + range * vy;
        String str = "";
        i = xx;
        j = yy;
        while (!inside(i, xb, xa) || !inside(j, yb, ya)) {
            i += vx;
            j += vy;
        }
        while (true) {
            str = str + String.valueOf(vcell[i][j]);
            if (str.length() == 5) {
                End(str);
                str = str.substring(1, 5);
            }
            i += vx;
            j += vy;
            if (!inB(i, j) || !inside(i, xb, xa) || !inside(j, yb, ya) || win != 0) {
                break;
            }
        }
    }

    private boolean inB(int i, int j) {
        if (i < 0 || i > max - 1 || j < 0 || j > max - 1) {
            return false;
        }
        return true;
    }

    private void End(String str) {
        switch (str) {
            case "11111":
                win = 1;
                break;
            case "22222":
                win = 2;
                break;
            default:
                break;
        }
    }

    private boolean inside(int i, int xb, int xa) {
        return (i - xb) * (i - xa) <= 0;
    }
    private void create_game() {
        fMove = true;
        win = 0;
        for (int i = 0; i < max; i++) {
            for (int j = 0; j < max; j++) {
                cell[i][j].setImageDrawable(dcell[0]);
                vcell[i][j] = 0;
            }
        }
    }
    private void loadResource() {
        dcell[3]= context.getResources().getDrawable(R.drawable.cell_bg);
        dcell[0] = null;
        dcell[1] = context.getResources().getDrawable(R.drawable.pcircle);
        dcell[2] = context.getResources().getDrawable(R.drawable.ccross);
    }
    private boolean ckclicked;
    @SuppressLint("NewApi")
    private void createBoard() {
        int sizeofCell = Math.round(Screen() / max);
        LinearLayout.LayoutParams prow = new LinearLayout.LayoutParams(sizeofCell * max, sizeofCell);
        LinearLayout.LayoutParams pcell = new LinearLayout.LayoutParams(sizeofCell, sizeofCell);
        LinearLayout Board = (LinearLayout) findViewById(R.id.Board);
        for (int i = 0; i < max; i++) {
            LinearLayout row = new LinearLayout(context);
            for (int j = 0; j < max; j++) {
                cell[i][j] = new ImageView(context);
                //cell[i][j].setBackground(dcell[3]);
                final int x = i;
                final int y = j;
                cell[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (vcell[x][y] == 0) {
                            if (turnP == 1 || !ckclicked) {

                                ckclicked = true;
                                xSet = x;
                                ySet = y;
                                make_move();
                            }
                        }
                    }
                });
                row.addView(cell[i][j], pcell);
            }
            Board.addView(row, prow);
        }
    }

    private float Screen() {
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        return dm.widthPixels;
    }

    private int Evaluation(String str, int play) {
        int b1 = 1;
        int b2 = 1;
        if (play == 1) {
            b1 = 2;
            b2 = 1;
        } else {
            b1 = 1;
            b2 = 2;
        }
        switch (str) {
            case "111110":
                return b1 * 100000000;
            case "011111":
                return b1 * 100000000;
            case "211111":
                return b1 * 100000000;
            case "111112":
                return b1 * 100000000;
            case "011110":
                return b1 * 10000000;
            case "101110":
                return b1 * 1002;
            case "011101":
                return b1 * 1002;
            case "011112":
                return b1 * 1000;
            case "011100":
                return b1 * 102;
            case "001110":
                return b1 * 102;
            case "210111":
                return b1 * 100;
            case "211110":
                return b1 * 100;
            case "211011":
                return b1 * 100;
            case "211101":
                return b1 * 100;
            case "010100":
                return b1 * 10;
            case "011000":
                return b1 * 10;
            case "001100":
                return b1 * 10;
            case "000110":
                return b1 * 10;
            case "211000":
                return b1 * 1;
            case "201100":
                return b1 * 1;
            case "200110":
                return b1 * 1;
            case "200011":
                return b1 * 1;
            case "222220":
                return b2 * -100000000;
            case "022222":
                return b2 * -100000000;
            case "122222":
                return b2 * -100000000;
            case "222221":
                return b2 * -100000000;
            case "022220":
                return b2 * -10000000;
            case "202220":
                return b2 * -1002;
            case "022202":
                return b2 * -1002;
            case "022221":
                return b2 * -1000;
            case "022200":
                return b2 * -102;
            case "002220":
                return b2 * -102;
            case "120222":
                return b2 * -100;
            case "122220":
                return b2 * -100;
            case "122022":
                return b2 * -100;
            case "122202":
                return b2 * -100;
            case "020200":
                return b2 * -10;
            case "022000":
                return b2 * -10;
            case "002200":
                return b2 * -10;
            case "000220":
                return b2 * -10;
            case "122000":
                return b2 * -1;
            case "102200":
                return b2 * -1;
            case "100220":
                return b2 * -1;
            case "100022":
                return b2 * -1;
            default:
                break;
        }
        return 0;
    }
}

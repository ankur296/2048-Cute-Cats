package corp.seedling.game2048.cute.cats.ui;

import java.util.ArrayList;


public class AnimGrid {
    public ArrayList<AnimCell>[][] field;
    int activeAnimations = 0;
    boolean oneMoreFrame = false;
    public ArrayList<AnimCell> globalAnimation = new ArrayList<AnimCell>();
    public AnimGrid(int x, int y) {
        field = new ArrayList[x][y];

        for (int xx = 0; xx < x; xx++) {
            for (int yy = 0; yy < y; yy++) {
                field[xx][yy] = new ArrayList<AnimCell>();
            }
        }
    }

    public void startAnimation(int x, int y, int animationType, long length, long delay, int[] extras) {
        AnimCell animationToAdd = new AnimCell(x, y, animationType, length, delay, extras);
        if (x == -1 && y == -1) {
            globalAnimation.add(animationToAdd);
        } else {
            field[x][y].add(animationToAdd);
        }
        activeAnimations = activeAnimations + 1;
    }

    public void tickAll(long timeElapsed) {
        ArrayList<AnimCell> cancelledAnimations = new ArrayList<AnimCell>();
        for (AnimCell animation : globalAnimation) {
            animation.tick(timeElapsed);
            if (animation.animationDone()) {
                cancelledAnimations.add(animation);
                activeAnimations = activeAnimations - 1;
            }
        }

        for (ArrayList<AnimCell>[] array : field) {
            for (ArrayList<AnimCell> list : array) {
                for (AnimCell animation : list) {
                    animation.tick(timeElapsed);
                    if (animation.animationDone()) {
                        cancelledAnimations.add(animation);
                        activeAnimations = activeAnimations - 1;
                    }
                }
            }
        }

        for (AnimCell animation : cancelledAnimations) {
            cancelAnimation(animation);
        }
    }

    public boolean isAnimationActive() {
        if (activeAnimations != 0) {
            oneMoreFrame = true;
            return true;
        } else if (oneMoreFrame) {
            oneMoreFrame = false;
            return true;
        } else {
            return false;
        }
    }

    public ArrayList<AnimCell> getAnimationCell(int x, int y) {
        return field[x][y];
    }

    public void cancelAnimations() {
        for (ArrayList<AnimCell>[] array : field) {
            for (ArrayList<AnimCell> list : array) {
                list.clear();
            }
        }
        globalAnimation.clear();
        activeAnimations = 0;
    }

    public void cancelAnimation(AnimCell animation) {
        if (animation.getX() == -1 && animation.getY() == -1) {
            globalAnimation.remove(animation);
        } else {
            field[animation.getX()][animation.getY()].remove(animation);
        }
    }

}

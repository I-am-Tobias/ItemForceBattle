package samann.itemforcebattle;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class Timer {
    private int ticksLeft;
    private final Runnable onEnd;
    private final Runnable onTick;
    private final BukkitTask tickTask;
    private boolean paused = false;

    public Timer(int ticks, Runnable onEnd, Runnable onTick){
        ticksLeft = ticks;
        this.onEnd = onEnd;
        this.onTick = onTick;

        tickTask = new BukkitRunnable() {
            @Override
            public void run() {
                if(!paused) tick();
            }
        }.runTaskTimer(ItemForceBattle.instance, 0, 1);
    }

    void tick(){
        ticksLeft--;
        if(ticksLeft == 0){
            onEnd.run();
            tickTask.cancel();
            return;
        }
        onTick.run();
    }

    public boolean isRunning(){
        return ticksLeft > 0;
    }
    public int getTicksLeft(){
        return ticksLeft;
    }
    public void pause(){
        paused = true;
    }
    public void resume(){
        paused = false;
    }
    public boolean isPaused(){
        return paused;
    }
    public void stop(){
        ticksLeft = 0;
        tickTask.cancel();
    }

}

package com.scottrealapps.calculater;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.scottrealapps.calculater.d2.BouncyScene;
import com.scottrealapps.calculater.d2.CrimeScene;

/**
 * This Activity contains either a A2DView or a Another2DView,depending on
 * arguments passed in.
 */
public class CanvasActivity extends AppCompatActivity implements CrimeScene.ScoreListener {
    //  well, this is mis-named...
    public static final String INTENT_DOUBLE_BUFFER = "doubleBuffer";
    //  ehh, so is this one.
    public static final String INTENT_SHOOTY = "shooty";

    private CrimeScene crimeScene = null;
    private TextView scoreStuff = null;
    private SceneUpdateThread sceneUpdateThread = null;

//seems like the wrong place for this
    private Runnable scoreUpdater = new Runnable() {
        @Override
        public void run() {
            if (scoreStuff != null) {
                scoreStuff.setText("Score: " + crimeScene.getScore() +
                        " Shots " + crimeScene.getShotCount() +
                        " GH " + crimeScene.getGoodHits() +
                        " BH " + crimeScene.getBadHits() +
                        " GE " + crimeScene.getGoodEscapes() +
                        " BE " + crimeScene.getBadEscapes());
            }
        }
    };

    @Override
    public void scoreChanged(CrimeScene scene) {
        runOnUiThread(scoreUpdater);
    }

    //  This runs the timer which calls MyView.updateScene() regularly.
    //  Copied from SnakeView.java under <SDL>/samples/android-21/legacy/Snake.
    //  THIS CAN BE DELETED if we get rid of A2DView.
    private class AnimationHandler extends Handler {
        //  this controls how fast we'll update our scene: sleeping 100ms between
        //  runs = about 10 times per second.
        private long sleepMS = 33L;
        private final A2DView view;
        AnimationHandler(A2DView view) {
            this.view = view;
        }

        @Override
        public void handleMessage(Message msg) {
            view.updateScene();
            view.invalidate();  //  OK?  not postInvalidate() ?
            sleep(sleepMS);
        }
        public void sleep(long sleepMS) {
            removeMessages(0);
            sendMessageDelayed(obtainMessage(0), sleepMS);
        }
    }
    private AnimationHandler animationTimer;// = new AnimationHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String instructions = null;
        if ((intent != null) && (intent.getBooleanExtra(INTENT_DOUBLE_BUFFER, false))) {
            setContentView(R.layout.activity_canvas2);
            Another2DView view = (Another2DView) (findViewById(R.id.myView));
            view.setScene(new BouncyScene(this));
            view.setupListeners();
            sceneUpdateThread = new SceneUpdateThread(view);
            instructions = getResources().getString(R.string.instructions_slap);
        } else if ((intent != null) && (intent.getBooleanExtra(INTENT_SHOOTY, false))) {
            setContentView(R.layout.activity_canvas3);
            Another2DView view = (Another2DView) (findViewById(R.id.myView));
            view.setScene(crimeScene = new CrimeScene(this));
            view.setupListeners();
            sceneUpdateThread = new SceneUpdateThread(view);
            instructions = getResources().getString(R.string.instructions_shoot);
            scoreStuff = (TextView)(findViewById(R.id.scoreStuff));
            crimeScene.addScoreListener(this);
scoreChanged(crimeScene);
        } else {
            setContentView(R.layout.activity_canvas);
            A2DView view = (A2DView)(findViewById(R.id.myView));
            animationTimer = new AnimationHandler(view);
            animationTimer.handleMessage(null);  //  start the animation running
            instructions = getResources().getString(R.string.instructions_slap);
        }
        if (instructions != null) {
            Toast.makeText(this, instructions, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStop() {
        if (crimeScene != null) crimeScene.onStop();
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (sceneUpdateThread != null) {
            sceneUpdateThread.setPaused(true);
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Closing Activity")
                    .setMessage("Are you sure you want to close this activity?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sceneUpdateThread.setPaused(false);
                            sceneUpdateThread.setRunning(false);
                            finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sceneUpdateThread.setPaused(false);
//                            finish();
                        }
                    })
                    .show();
        }
//        Toast.makeText(this, "doing NOTHING", Toast.LENGTH_SHORT).show();
//        super.onBackPressed();
    }


}

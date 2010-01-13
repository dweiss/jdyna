// Imported from: JMonkeyEngine SVN; StandardGame.java 4501 2009-07-13 10:42:01Z
// [with changes]

package org.jdyna.view.jme;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import com.jme.app.AbstractGame;
import com.jme.image.Image;
import com.jme.input.InputSystem;
import com.jme.input.MouseInput;
import com.jme.input.joystick.JoystickInput;
import com.jme.math.Vector3f;
import com.jme.renderer.Camera;
import com.jme.renderer.ColorRGBA;
import com.jme.system.DisplaySystem;
import com.jme.system.GameSettings;
import com.jme.system.PreferencesGameSettings;
import com.jme.util.GameTaskQueue;
import com.jme.util.GameTaskQueueManager;
import com.jme.util.TextureManager;
import com.jme.util.Timer;
import com.jmex.audio.AudioSystem;
import com.jmex.game.state.GameStateManager;

/**
 * Initialization, main rendering loop and handling of interaction with the JME library.
 */
final class StandardGame extends AbstractGame implements Runnable
{
    private static final Logger logger = Logger.getLogger(StandardGame.class.getName());

    /** Game name. */
    private String gameName;

    Thread gameThread;
    private Image [] icons;

    private Timer timer;
    private Camera camera;
    private ColorRGBA backgroundColor;
    
    /** OpenGL initialization thread/ start barrier. */
    private CountDownLatch startLatch;

    /**
     * @see AbstractGame#getNewSettings()
     */
    protected GameSettings getNewSettings()
    {
        /*
         * TODO: we may wish to modify this method
         */
        boolean newNode = true;
        Preferences userPrefsRoot = Preferences.userRoot();
        try
        {
            newNode = !userPrefsRoot.nodeExists(gameName);
        }
        catch (BackingStoreException bse)
        {
        }

        return new PreferencesGameSettings(userPrefsRoot.node(gameName), newNode,
            "game-defaults.properties");
    }

    public StandardGame(String gameName, GameSettings settings)
    {
        this.gameName = gameName;
        this.settings = settings;
        backgroundColor = ColorRGBA.black.clone();

        if (this.settings == null)
        {
            // TODO: we may wish to enforce passing non-null settings from the outside
            // here.
            this.settings = getNewSettings();
        }
    }

    public void start()
    {
        gameThread = new Thread(this);
        gameThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            public void uncaughtException(Thread thread, Throwable t)
            {
                logger.log(Level.SEVERE, 
                    "Main game loop broken by uncaught exception.", t);

                startLatch.countDown(); // Just in case we crash before entering the main loop.
                shutdown();
                cleanup();
                quit();
            }
        });

        // Assign a name to the thread
        gameThread.setName(gameName + " [3D]");
        gameThread.start();

        // Wait for the OpenGL thread's initialization.
        startLatch = new CountDownLatch(1);
        try
        {
            startLatch.await();
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException("Unreachable code.", e);
        }
    }

    public void run()
    {
        initSystem();
        assertDisplayCreated();

        // Default the mouse cursor to off
        MouseInput.get().setCursorVisible(false);

        initGame();
        timer = Timer.getTimer();

        // Configure frame rate
        int preferredFPS = settings.getFramerate();
        long preferredTicksPerFrame = -1;
        long frameStartTick = -1;
        long frames = 0;
        long frameDurationTicks = -1;
        if (preferredFPS >= 0)
        {
            preferredTicksPerFrame = Math.round((float) timer.getResolution()
                / (float) preferredFPS);
        }

        // Enter the main game loop
        startLatch.countDown();

        // FIXME: finished is not volatile in AbstractGame...
        while ((!finished) && (!display.isClosing()))
        {
            // Fixed framerate Start
            if (preferredTicksPerFrame >= 0)
            {
                frameStartTick = timer.getTime();
            }

            timer.update();
            final float tpf = timer.getTimePerFrame();

            InputSystem.update();
            update(tpf);
            render(tpf);
            display.getRenderer().displayBackBuffer();

            // Fixed framerate End
            if (preferredTicksPerFrame >= 0)
            {
                frames++;
                frameDurationTicks = timer.getTime() - frameStartTick;
                while (frameDurationTicks < preferredTicksPerFrame)
                {
                    long sleepTime = ((preferredTicksPerFrame - frameDurationTicks) * 1000)
                        / timer.getResolution();
                    try
                    {
                        Thread.sleep(sleepTime);
                    }
                    catch (InterruptedException exc)
                    {
                        logger.log(Level.SEVERE,
                            "Interrupted while sleeping in fixed-framerate", exc);
                    }
                    frameDurationTicks = timer.getTime() - frameStartTick;
                }
                if (frames == Long.MAX_VALUE) frames = 0;
            }
        }
        cleanup();
        quit();
    }

    protected void initSystem()
    {
        // Configure Joystick
        if (JoystickInput.getProvider() == null)
        {
            JoystickInput.setProvider(InputSystem.INPUT_SYSTEM_LWJGL);
        }

        display = DisplaySystem.getDisplaySystem(settings.getRenderer());
        displayMins();

        display.setTitle(gameName);
        if (icons != null)
        {
            display.setIcon(icons);
        }

        display.createWindow(settings.getWidth(), settings.getHeight(), settings
            .getDepth(), settings.getFrequency(), settings.isFullscreen());

        camera = display.getRenderer().createCamera(display.getWidth(),
            display.getHeight());
        display.getRenderer().setBackgroundColor(backgroundColor);

        // Setup Vertical Sync if enabled
        display.setVSyncEnabled(settings.isVerticalSync());

        // Configure Camera
        cameraPerspective();
        cameraFrame();
        camera.update();
        display.getRenderer().setCamera(camera);

        if ((settings.isMusic()) || (settings.isSFX()))
        {
            initSound();
        }
    }

    protected void initSound()
    {
        AudioSystem.getSystem().getEar().trackOrientation(camera);
        AudioSystem.getSystem().getEar().trackPosition(camera);
    }

    private void displayMins()
    {
        display.setMinDepthBits(settings.getDepthBits());
        display.setMinStencilBits(settings.getStencilBits());
        display.setMinAlphaBits(settings.getAlphaBits());
        display.setMinSamples(settings.getSamples());
    }

    private void cameraPerspective()
    {
        camera.setFrustumPerspective(45.0f, (float) display.getWidth()
            / (float) display.getHeight(), 1.0f, 1000.0f);
        camera.setParallelProjection(false);
        camera.update();
    }

    private void cameraFrame()
    {
        Vector3f loc = new Vector3f(0.0f, 0.0f, 25.0f);
        Vector3f left = new Vector3f(-1.0f, 0.0f, 0.0f);
        Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
        Vector3f dir = new Vector3f(0.0f, 0.0f, -1.0f);
        camera.setFrame(loc, left, up, dir);
    }

    public void resetCamera()
    {
        cameraFrame();
    }

    protected void initGame()
    {
        // Create the GameStateManager
        GameStateManager.create();
    }

    protected void update(float interpolation)
    {
        // Execute updateQueue item
        GameTaskQueueManager.getManager().getQueue(GameTaskQueue.UPDATE).execute();

        // Update the GameStates
        GameStateManager.getInstance().update(interpolation);

        // Update music/sound
        if ((settings.isMusic()) || (settings.isSFX()))
        {
            AudioSystem.getSystem().update();
        }
    }

    protected void render(float interpolation)
    {
        display.getRenderer().clearBuffers();

        // Execute renderQueue item
        GameTaskQueueManager.getManager().getQueue(GameTaskQueue.RENDER).execute();

        // Render the GameStates
        GameStateManager.getInstance().render(interpolation);
    }

    public void reinit()
    {
        reinitAudio();
        reinitVideo();
    }

    public void reinitAudio()
    {
        if (AudioSystem.isCreated())
        {
            AudioSystem.getSystem().cleanup();
        }
    }

    public void reinitVideo()
    {
        GameTaskQueueManager.getManager().update(new Callable<Object>()
        {
            public Object call() throws Exception
            {
                displayMins();

                display
                    .recreateWindow(settings.getWidth(), settings.getHeight(), settings
                        .getDepth(), settings.getFrequency(), settings.isFullscreen());
                camera = display.getRenderer().createCamera(display.getWidth(),
                    display.getHeight());
                display.getRenderer().setBackgroundColor(backgroundColor);
                if ((settings.isMusic()) || (settings.isSFX()))
                {
                    initSound();
                }
                return null;
            }
        });
    }

    public void recreateGraphicalContext()
    {
        reinit();
    }

    @Override
    protected void cleanup()
    {
        GameStateManager.getInstance().cleanup();

        DisplaySystem.getDisplaySystem().getRenderer().cleanup();
        TextureManager.doTextureCleanup();
        TextureManager.clearCache();

        JoystickInput.destroyIfInitalized();
        if (AudioSystem.isCreated())
        {
            AudioSystem.getSystem().cleanup();
        }
    }

    @Override
    protected void quit()
    {
        if (display != null)
        {
            display.reset();
            display.close();
        }
    }

    /**
     * The internally used <code>DisplaySystem</code> for this instance of
     * <code>StandardGame</code>
     * 
     * @return DisplaySystem
     * @see DisplaySystem
     */
    public DisplaySystem getDisplay()
    {
        return display;
    }

    /**
     * The internally used <code>Camera</code> for this instance of
     * <code>StandardGame</code>.
     * 
     * @return Camera
     * @see Camera
     */
    public Camera getCamera()
    {
        return camera;
    }

    /**
     * The <code>GameSettings</code> implementation being utilized in this instance of
     * <code>StandardGame</code>.
     * 
     * @return GameSettings
     * @see GameSettings
     */
    public GameSettings getSettings()
    {
        return settings;
    }

    /**
     * Override the background color defined for this game. The reinit() method must be
     * invoked if the game is currently running before this will take effect.
     * 
     * @param backgroundColor
     */
    public void setBackgroundColor(ColorRGBA backgroundColor)
    {
        this.backgroundColor = backgroundColor;
    }

    /**
     * Gracefully shutdown the main game loop thread. This is a synonym for the finish()
     * method but just sounds better.
     * 
     * @see #finish()
     */
    public void shutdown()
    {
        finish();
    }

    /**
     * Convenience method to let you know if the thread you're in is the OpenGL thread
     * 
     * @return true if, and only if, the current thread is the OpenGL thread
     */
    public boolean inGLThread()
    {
        if (Thread.currentThread() == gameThread)
        {
            return true;
        }
        return false;
    }

    /**
     * Convenience method that will make sure <code>callable</code> is executed in the
     * OpenGL thread. If it is already in the OpenGL thread when this method is invoked it
     * will be executed and returned immediately. Otherwise, it will be put into the
     * GameTaskQueue and executed in the next update. This is a blocking method and will
     * wait for the successful return of <code>callable</code> before returning.
     * 
     * @param <T>
     * @param callable
     * @return result of callable.get()
     * @throws Exception
     */
    public <T> T executeInGL(Callable<T> callable) throws Exception
    {
        if (inGLThread())
        {
            return callable.call();
        }
        Future<T> future = GameTaskQueueManager.getManager().update(callable);
        return future.get();
    }
}

/*
 * Copyright (c) 2003-2009 jMonkeyEngine All rights reserved. Redistribution and use in
 * source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met: * Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following disclaimer. *
 * Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution. * Neither the name of 'jMonkeyEngine' nor the names of
 * its contributors may be used to endorse or promote products derived from this software
 * without specific prior written permission. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT
 * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

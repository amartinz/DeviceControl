/*
 * This file is part of the RootTools Project: http://code.google.com/p/roottools/
 *
 * Copyright (c) 2012 Stephen Erickson, Chris Ravenscroft, Dominik Schuermann, Adam Shanks
 *
 * This code is dual-licensed under the terms of the Apache License Version 2.0 and
 * the terms of the General Public License (GPL) Version 2.
 * You may use this code according to either of these licenses as is most appropriate
 * for your project on a case-by-case basis.
 *
 * The terms of each license can be found in the root directory of this project's repository as
 * well as at:
 *
 * * http://www.apache.org/licenses/LICENSE-2.0
 * * http://www.gnu.org/licenses/gpl-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under these Licenses is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See each License for the specific language governing permissions and
 * limitations under that License.
 */

package com.stericson.roottools;

import android.text.TextUtils;
import android.util.Log;

import com.stericson.roottools.containers.Mount;
import com.stericson.roottools.containers.Permissions;
import com.stericson.roottools.containers.Symlink;
import com.stericson.roottools.exceptions.RootDeniedException;
import com.stericson.roottools.execution.Shell;
import com.stericson.roottools.internal.RootToolsInternalMethods;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

public final class RootTools {

    /**
     * This class is the gateway to every functionality within the RootTools library.The developer
     * should only have access to this class and this class only.This means that this class should
     * be the only one to be public.The rest of the classes within this library must not have the
     * public modifier.
     * <p/>
     * All methods and Variables that the developer may need to have access to should be here.
     * <p/>
     * If a method, or a specific functionality, requires a fair amount of code, or work to be done,
     * then that functionality should probably be moved to its own class and the call to it done
     * here.For examples of this being done, look at the remount functionality.
     */

    private static RootToolsInternalMethods rim = null;

    public static void setRim(final RootToolsInternalMethods rim) { RootTools.rim = rim; }

    private static RootToolsInternalMethods getInternals() {
        if (rim == null) { RootToolsInternalMethods.getInstance(); }
        return rim;
    }

    // --------------------
    // # Public Variables #
    // --------------------

    public static boolean debugMode = false;
    public static final List<String> lastFoundBinaryPaths = new ArrayList<>();
    public static String utilPath;

    /**
     * Setting this to false will disable the handler that is used
     * by default for the 3 callback methods for Command.
     * <p/>
     * By disabling this all callbacks will be called from a thread other than
     * the main UI thread.
     */
    public static final boolean handlerEnabled = true;


    /**
     * Setting this will change the default command timeout.
     * <p/>
     * The default is 20000ms
     */
    public static final int default_Command_Timeout = 20000;


    // ---------------------------
    // # Public Variable Getters #
    // ---------------------------

    // ------------------
    // # Public Methods #
    // ------------------

    /**
     * This will check a given binary, determine if it exists and determine that it has either the
     * permissions 755, 775, or 777.
     *
     * @param util Name of the utility to check.
     * @return boolean to indicate whether the binary is installed and has appropriate permissions.
     */
    public static boolean checkUtil(final String util) {
        return getInternals().checkUtil(util);
    }

    /**
     * This will close all open shells.
     */
    public static void closeAllShells() { Shell.closeAll(); }

    /**
     * This will close either the root shell or the standard shell depending on what you specify.
     *
     * @param root a <code>boolean</code> to specify whether to close the root shell or the
     *             standard shell.
     */
    public static void closeShell(final boolean root) {
        if (root) { Shell.closeRootShell(); } else { Shell.closeShell(); }
    }

    /**
     * Use this to check whether or not a file exists on the filesystem.
     *
     * @param file String that represent the file, including the full path to the
     *             file and its name.
     * @return a boolean that will indicate whether or not the file exists.
     */
    public static boolean exists(final String file) { return getInternals().exists(file); }

    /**
     * @param binaryName String that represent the binary to find.
     * @return <code>true</code> if the specified binary was found. Also, the path the binary was
     * found at can be retrieved via the variable lastFoundBinaryPath, if the binary was
     * found in more than one location this will contain all of these locations.
     */
    public static boolean findBinary(final String binaryName) {
        return getInternals().findBinary(binaryName);
    }

    /**
     * @param path String that represents the path to the Busybox binary you want to retrieve the
     *             version of.
     * @return BusyBox version is found, "" if not found.
     */
    public static String getBusyBoxVersion(final String path) {
        return getInternals().getBusyBoxVersion(path);
    }

    /**
     * @return BusyBox version is found, "" if not found.
     */
    public static String getBusyBoxVersion() { return RootTools.getBusyBoxVersion(""); }

    /**
     * This will return an List of Strings. Each string represents an applet available from BusyBox.
     * <p/>
     *
     * @return <code>null</code> If we cannot return the list of applets.
     */
    public static List<String> getBusyBoxApplets() throws Exception {
        return RootTools.getBusyBoxApplets("");
    }

    /**
     * This will return an List of Strings. Each string represents an applet available from BusyBox.
     * <p/>
     *
     * @param path Path to the busybox binary that you want the list of applets from.
     * @return <code>null</code> If we cannot return the list of applets.
     */
    public static List<String> getBusyBoxApplets(final String path) throws Exception {
        return getInternals().getBusyBoxApplets(path);
    }

    /**
     * This will open or return, if one is already open, a custom shell,
     * you are responsible for managing the shell, reading the output
     * and for closing the shell when you are done using it.
     *
     * @param shellPath a <code>String</code> to Indicate the path to the shell that you want to
     *                  open.
     * @param timeout   an <code>int</code> to Indicate the length of time before giving up on
     *                  opening a shell.
     * @throws java.util.concurrent.TimeoutException
     * @throws com.stericson.roottools.exceptions.RootDeniedException
     * @throws java.io.IOException
     */
    public static Shell getCustomShell(final String shellPath, final int timeout)
            throws IOException, TimeoutException, RootDeniedException {
        return Shell.startCustomShell(shellPath, timeout);
    }

    /**
     * This will open or return, if one is already open, a custom shell,
     * you are responsible for managing the shell, reading the output
     * and for closing the shell when you are done using it.
     *
     * @param shellPath a <code>String</code> to Indicate the path to the shell that you want to
     *                  open.
     * @throws java.util.concurrent.TimeoutException
     * @throws com.stericson.roottools.exceptions.RootDeniedException
     * @throws java.io.IOException
     */
    public static Shell getCustomShell(final String shellPath)
            throws IOException, TimeoutException, RootDeniedException {
        return RootTools.getCustomShell(shellPath, 10000);
    }

    /**
     * @param file String that represent the file, including the full path to the file and its name.
     * @return An instance of the class permissions from which you can get the permissions of the
     * file or if the file could not be found or permissions couldn't be determined then
     * permissions will be null.
     */
    public static Permissions getFilePermissionsSymlinks(final String file) {
        return getInternals().getFilePermissionsSymlinks(file);
    }

    /**
     * This method will return the inode number of a file. This method is dependent on having a
     * version of
     * ls that supports the -i parameter.
     *
     * @param file path to the file that you wish to return the inode number
     * @return String The inode number for this file or "" if the inode number could not be found.
     */
    public static String getInode(final String file) { return getInternals().getInode(file); }

    /**
     * This will return an ArrayList of the class Mount. The class mount contains the following
     * property's: device mountPoint type flags
     * <p/>
     * These will provide you with any information you need to work with the mount points.
     *
     * @return <code>ArrayList<Mount></code> an ArrayList of the class Mount.
     * @throws Exception if we cannot return the mount points.
     */
    public static ArrayList<Mount> getMounts() throws Exception {
        return getInternals().getMounts();
    }

    /**
     * This will tell you how the specified mount is mounted. rw, ro, etc...
     * <p/>
     *
     * @param path The mount you want to check
     * @return <code>String</code> What the mount is mounted as.
     * @throws Exception if we cannot determine how the mount is mounted.
     */
    public static String getMountedAs(final String path) throws Exception {
        return getInternals().getMountedAs(path);
    }

    /**
     * This will return the environment variable PATH
     *
     * @return <code>List<String></code> A List of Strings representing the environment variable
     * $PATH
     */
    public static List<String> getPath() { return Arrays.asList(System.getenv("PATH").split(":")); }

    /**
     * This will open or return, if one is already open, a shell,
     * you are responsible for managing the shell, reading the output
     * and for closing the shell when you are done using it.
     *
     * @param root    a <code>boolean</code> to Indicate whether or not you want to open a root
     *                shell or a standard shell
     * @param timeout an <code>int</code> to Indicate the length of time to wait before giving up
     *                on opening a shell.
     * @throws java.util.concurrent.TimeoutException
     * @throws com.stericson.roottools.exceptions.RootDeniedException
     * @throws java.io.IOException
     */
    public static Shell getShell(final boolean root, final int timeout)
            throws IOException, TimeoutException, RootDeniedException {
        if (root) { return Shell.startRootShell(timeout); } else {
            return Shell.startShell(timeout);
        }
    }

    /**
     * This will open or return, if one is already open, a shell,
     * you are responsible for managing the shell, reading the output
     * and for closing the shell when you are done using it.
     *
     * @param root a <code>boolean</code> to Indicate whether or not you want to open a root
     *             shell or a standard shell
     * @throws java.util.concurrent.TimeoutException
     * @throws com.stericson.roottools.exceptions.RootDeniedException
     * @throws java.io.IOException
     */
    public static Shell getShell(final boolean root)
            throws IOException, TimeoutException, RootDeniedException {
        return RootTools.getShell(root, 25000);
    }

    /**
     * Get the space for a desired partition.
     *
     * @param path The partition to find the space for.
     * @return the amount if space found within the desired partition. If the space was not found
     * then the value is -1
     * @throws java.util.concurrent.TimeoutException
     */
    public static long getSpace(final String path) { return getInternals().getSpace(path); }

    /**
     * This will return a String that represent the symlink for a specified file.
     * <p/>
     *
     * @param file path to the file to get the Symlink for. (must have absolute path)
     * @return <code>String</code> a String that represent the symlink for a specified file or an
     * empty string if no symlink exists.
     */
    public static String getSymlink(final String file) { return getInternals().getSymlink(file); }

    /**
     * This will return an ArrayList of the class Symlink. The class Symlink contains the following
     * property's: path SymplinkPath
     * <p/>
     * These will provide you with any Symlinks in the given path.
     *
     * @param path path to search for Symlinks.
     * @return <code>ArrayList<Symlink></code> an ArrayList of the class Symlink.
     * @throws Exception if we cannot return the Symlinks.
     */
    public static ArrayList<Symlink> getSymlinks(final String path) throws Exception {
        return getInternals().getSymlinks(path);
    }

    /**
     * This will return to you a string to be used in your shell commands which will represent the
     * valid working toolbox with correct permissions. For instance, if Busybox is available it will
     * return "busybox", if busybox is not available but toolbox is then it will return "toolbox"
     *
     * @return String that indicates the available toolbox to use for accessing applets.
     */
    public static String getWorkingToolbox() { return getInternals().getWorkingToolbox(); }

    /**
     * Checks if there is enough Space on SDCard
     *
     * @param updateSize size to Check (long)
     * @return <code>true</code> if the Update will fit on SDCard, <code>false</code> if not enough
     * space on SDCard. Will also return <code>false</code>, if the SDCard is not mounted as
     * read/write
     */
    public static boolean hasEnoughSpaceOnSdCard(final long updateSize) {
        return getInternals().hasEnoughSpaceOnSdCard(updateSize);
    }

    /**
     * This will let you know if an applet is available from BusyBox
     * <p/>
     *
     * @param applet The applet to check for.
     * @param path   Path to the busybox binary that you want to check. (do not include binary name)
     * @return <code>true</code> if applet is available, false otherwise.
     */
    public static boolean isAppletAvailable(final String applet, final String path) {
        return getInternals().isAppletAvailable(applet, path);
    }

    /**
     * This will let you know if an applet is available from BusyBox
     * <p/>
     *
     * @param applet The applet to check for.
     * @return <code>true</code> if applet is available, false otherwise.
     */
    public static boolean isAppletAvailable(final String applet) {
        return RootTools.isAppletAvailable(applet, "");
    }

    /**
     * @return <code>true</code> if your app has been given root access.
     */
    public static boolean isAccessGiven() { return getInternals().isAccessGiven(); }

    /**
     * @return <code>true</code> if BusyBox was found.
     */
    public static boolean isBusyboxAvailable() { return findBinary("busybox"); }

    /**
     * This method can be used to to check if a process is running
     *
     * @param processName name of process to check
     * @return <code>true</code> if process was found
     * @throws java.util.concurrent.TimeoutException (Could not determine if the process is running)
     */
    public static boolean isProcessRunning(final String processName) {
        //TODO convert to new shell
        return getInternals().isProcessRunning(processName);
    }

    /**
     * @return <code>true</code> if su was found.
     */
    public static boolean isRootAvailable() { return findBinary("su"); }

    /**
     * This will launch the Android market looking for BusyBox
     */
    public static void offerBusyBox() {
        getInternals().offerBusyBox();
    }

    /**
     * This method allows you to output debug messages only when debugging is on. This will allow
     * you to add a debug option to your app, which by default can be left off for performance.
     * However, when you need debugging information, a simple switch can enable it and provide you
     * with detailed logging.
     * <p/>
     * This method handles whether or not to log the information you pass it depending whether or
     * not RootTools.debugMode is on. So you can use this and not have to worry about handling it
     * yourself.
     *
     * @param msg The message to output.
     */
    public static void log(final String msg) { log(null, msg, 3, null); }

    /**
     * This method allows you to output debug messages only when debugging is on. This will allow
     * you to add a debug option to your app, which by default can be left off for performance.
     * However, when you need debugging information, a simple switch can enable it and provide you
     * with detailed logging.
     * <p/>
     * This method handles whether or not to log the information you pass it depending whether or
     * not RootTools.debugMode is on. So you can use this and not have to worry about handling it
     * yourself.
     *
     * @param TAG Optional parameter to define the tag that the Log will use.
     * @param msg The message to output.
     */
    public static void log(final String TAG, final String msg) { log(TAG, msg, 3, null); }

    /**
     * This method allows you to output debug messages only when debugging is on. This will allow
     * you to add a debug option to your app, which by default can be left off for performance.
     * However, when you need debugging information, a simple switch can enable it and provide you
     * with detailed logging.
     * <p/>
     * This method handles whether or not to log the information you pass it depending whether or
     * not RootTools.debugMode is on. So you can use this and not have to worry about handling it
     * yourself.
     *
     * @param msg  The message to output.
     * @param type The type of log, 1 for verbose, 2 for error, 3 for debug
     * @param e    The exception that was thrown (Needed for errors)
     */
    public static void log(final String msg, final int type, final Exception e) {
        log(null, msg, type, e);
    }

    /**
     * This method allows you to output debug messages only when debugging is on. This will allow
     * you to add a debug option to your app, which by default can be left off for performance.
     * However, when you need debugging information, a simple switch can enable it and provide you
     * with detailed logging.
     * <p/>
     * This method handles whether or not to log the information you pass it depending whether or
     * not RootTools.debugMode is on. So you can use this and not have to worry about handling it
     * yourself.
     *
     * @param TAG  Optional parameter to define the tag that the Log will use.
     * @param msg  The message to output.
     * @param type The type of log, 1 for verbose, 2 for error, 3 for debug
     * @param e    The exception that was thrown (Needed for errors)
     */
    public static void log(String TAG, final String msg, final int type, final Exception e) {
        if (!debugMode || TextUtils.isEmpty(msg)) {
            return;
        }

        if (TAG == null) {
            TAG = Constants.TAG;
        }

        switch (type) {
            case 1:
                Log.v(TAG, msg);
                break;
            case 2:
                Log.e(TAG, msg, e);
                break;
            case 3:
                Log.d(TAG, msg);
                break;
        }
    }
}

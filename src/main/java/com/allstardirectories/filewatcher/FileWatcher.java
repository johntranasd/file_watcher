package com.allstardirectories.filewatcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.FileUtil;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;

/**
 * @author John Tran
 *         A very rudementary file watcher to copy, update and delete a file in the fromTo structure argument.
 */
public class FileWatcher {
    public static class FromTo {
        String from;
        String to;
        String newField;
    }

    private void test(){

    }
    private Collection<FromTo> resourceFromTo = Collections.emptyList();

    public FileWatcher(Collection<FromTo> resourceFromTo) {
        this.resourceFromTo = resourceFromTo;
    }

    public static void main(String[] args) throws FileSystemException {

        Collection<FromTo> fromToCollection = new ArrayList<FromTo>();
        if ( args.length >= 1 ) { //i.e /home/john.tran/polopoly/module-asd/velocity=/home/john.tran/polopoly/webapps/velocity,abc=cdf
            String[] combos = args[0].split(",");
            for ( String combo : combos ) {
                FromTo fromTo = new FromTo();
                String[] split = combo.split("=");

                fromTo.from = split[0];
                fromTo.to = split[1];
                fromToCollection.add(fromTo);
            }
        }
        FileWatcher fileWatcher = new FileWatcher(fromToCollection);
        fileWatcher.monitor();
    }

    public void monitor() throws FileSystemException {
        System.out.println("Starting monitor");
        Thread watchThread = new Thread() {
            @Override
            public void run() {
                try {
                    FileSystemManager fsManager = VFS.getManager();
                    for ( FromTo fromTo : resourceFromTo ) {

                        FileObject listendir = fsManager.resolveFile(fromTo.from);
                        DefaultFileMonitor fm = new DefaultFileMonitor(new CustomFileListener(fromTo));
                        fm.setRecursive(true);
                        System.out.println("Watching folder: " + fromTo.from + " will be copying changes to:" + fromTo.to);
                        System.out.flush();
                        fm.addFile(listendir);
                        fm.start();
                    }
                }
                catch ( Exception ex ) {
                    ex.printStackTrace();
                }

                //running forever
                while ( true ) {
                    try {
                        Thread.sleep(100);
                    }
                    catch ( InterruptedException e ) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        };
//        watchThread.setDaemon(true);
        watchThread.start();
    }
}

class CustomFileListener implements FileListener {

    private FileWatcher.FromTo fromTo;

    public CustomFileListener(FileWatcher.FromTo fromTo) {
        this.fromTo = fromTo;
    }

    @Override
    public void fileCreated(FileChangeEvent event) throws Exception {
        fileChangeHelper(event);
    }

    private void fileChangeHelper(FileChangeEvent event) throws IOException {
        String destination = formDestinationPath(event.getFile().getName().getPath());
        System.out.println(String.format("File changed:Updating file from %s, to %s", event.getFile().getName(), destination));
        FileObject destFileObject = VFS.getManager().resolveFile(destination);
        if ( event.getFile().getType() == FileType.FILE ) {
            FileUtil.copyContent(event.getFile(), destFileObject);
        }
    }

    @Override
    public void fileDeleted(FileChangeEvent event) throws Exception {

        String destination = formDestinationPath(event.getFile().getName().getPath());
        System.out.println(String.format("File deleting :Updating file from %s, to %s", event.getFile().getName(), destination));
        new File(destination).delete();
//        }
    }

    @Override
    public void fileChanged(FileChangeEvent event) throws Exception {
        fileChangeHelper(event);
    }

    public String formDestinationPath(String filePathChange) {
        if ( fromTo.from == null || filePathChange == null ) {
            throw new IllegalStateException("fromTo.from or filePathChange is null");
        }
        if ( filePathChange.startsWith(fromTo.from) ) {
            String relativeFilePath = filePathChange.substring(fromTo.from.length());
            return fromTo.to + relativeFilePath;
        }
        return filePathChange;
    }
}
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import sim.engine.SimState;
import java.util.ArrayDeque;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
/**
 *
 * @author ManuelLS
 */
public class DebugLogger implements sim.engine.Steppable{
    
    private final static long serialVersionUID = 1;
    
    private final int logLevel; //To make the levels easier we'll make it a > all items > loglevel will be printed. 
    //going to give the levels some space in case there are later sublevels...
    public static final int LOG_ALL = 0;
    public static final int LOG_DEBUG = 5;
    public static final int LOG_WARNINGS = 10;
    public static final int LOG_SEVERE = 20;
    private final BufferedWriter debugOut;
    private final ArrayDeque debugQueue;//an UNSYNCHRONIZED queue reported on javadocs to be faster than linked list.
    
    
    public void insertDebug(String log, int level){
        if(logLevel > level) return;
        DateFormat format= new SimpleDateFormat("yyyy-mm-dd h:m:s");
        log = format.format(Calendar.getInstance().getTime()) + ": " + log;        
        debugQueue.add(log);
    }
    
    @Override
    public void step(SimState state){
        if(logLevel == Integer.MAX_VALUE)return; //this shouldn't be scheduled if it's not logging, but just in case.  
        HIVMicroSim sim = (HIVMicroSim) state;
        //handle queue
        Object o;
        while((o = debugQueue.pollFirst()) != null){
            try{
                debugOut.newLine();
                debugOut.write((String)o);
            }catch(IOException e){
                System.err.println("Could not write to event report "+e.getLocalizedMessage()+"\n" + (String)o);
            }
        }
    }
    public void close(){
        if(logLevel == -1)return;
        try{
            debugOut.flush();
            debugOut.close();
        }catch(IOException e){
            System.err.println("Could not close! " + e.getLocalizedMessage());
        }
    }
    public DebugLogger(int level, String debugFile) throws IOException{
        logLevel = level;
        
        debugOut = new BufferedWriter(new FileWriter(debugFile, true),(8*1024)); // second argument T means will append if it exists.
        
        debugQueue = new ArrayDeque();
    }
    public DebugLogger(){
        logLevel = Integer.MAX_VALUE; // no int value higher. 
        debugOut = null;
        debugQueue = null;
    }
}

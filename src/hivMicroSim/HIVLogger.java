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
import hivMicroSim.Agent.Agent;

/**
 *
 * @author manuells
 */
public class HIVLogger implements sim.engine.Steppable{
    /*
    *This class creates (currently) two output log files. The first log file logs "events" and the second logs yearly data such as
    *incidence, prevalence, mortality, birth and death rates.
    
    ** May add a third to post "agent" information. (for analysis of rNot by agent features (e.g. faithfulness and condom usage)
    */
    private final static long serialVersionUID = 1;
    private final int logLevel; //the level for the logging. Level must be <  logLevel to be printed.
    private final BufferedWriter yearOut;
    private final BufferedWriter eventOut;
    private final BufferedWriter agentOut;
    
    private final ArrayDeque eventQueue;//an UNSYNCHRONIZED queue reported on javadocs to be faster than linked list.
    
    public final static int INFECT_MOTHERTOCHILD = 1;
    public final static int INFECT_HETERO = 0;
    public final static int LOG_ALL = 10;
    public final static int LOG_INFECT = 0;
    public final static int LOG_PROGRESSION = 1;
    public final static int LOG_DEATH = 2;
    public final static int LOG_CONCEPTION = 3;
    public final static int LOG_BIRTH = 4;
    
    private int year = 0;
    private int month = 0; // month 0 (first call to step) will be the initial pre simulation values
    private int turn = 0;
    //keeping yearly tallies since all these are reported and it
    //means we don't have to query all agents
    private int yearBirth = 0;
    private int yearDeath = 0;
    private int yearMortality = 0;
    private int yearInfect = 0;
    private int yearPrevalence = 0;
    private int yearLiving = 0; //the number of agents alive at the start of the year for rate calculations;
    private int CCR5Homozygous = 0;
    private int livingAgents = 0;
    
    //note that 
    
    
    @Override
    public void step(SimState state){
        if(logLevel == -1)return;    
        HIVMicroSim sim = (HIVMicroSim) state;
        //handle queue
        Object o;
        while((o = eventQueue.pollFirst()) != null){
            try{
                eventOut.newLine();
                eventOut.write((String)o);
            }catch(IOException e){
                System.err.println("Could not write to event report "+e.getLocalizedMessage()+"\n" + (String)o);
            }
        }
        //if it's on a year change, handle the year.
        turn++;
        month++;
        if(month > 12){
            String log = year + "\t"+ yearLiving + "\t" + yearInfect + "\t" + yearPrevalence + "\t" + yearMortality + "\t" + yearBirth + "\t" + yearDeath+ "\t" + CCR5Homozygous;
            try{
                yearOut.newLine();
                yearOut.write(log);
            }catch(IOException e){
                System.err.println("Could not write yearly report for year: " + year + " "+e.getLocalizedMessage()+"\n" + log);
            }
            yearLiving = livingAgents;
            yearBirth = yearDeath=yearMortality=yearInfect=0;
            year++;
            month = 1;
        }
    }
    public void close(){
        if(logLevel == -1)return;
        try{
            yearOut.flush();
            eventOut.flush();
            agentOut.flush();
            yearOut.close();
            eventOut.close();
            agentOut.close();
        }catch(IOException e){
            System.err.println("Could not close! " + e.getLocalizedMessage());
        }
    }
    public void insertInfection(int infectMode, int agent1, int agent2, int genotype, boolean newInfect){
        yearInfect ++;
        if(logLevel < LOG_INFECT) return;
        String log = "";
        if(infectMode == INFECT_HETERO){
            if(newInfect){
                yearPrevalence++;
                log = agent1 + "\tNew Heterosexual Infection\t" + genotype +"\t"+ agent2 +"\t"+ turn;
            }else{
                log = agent1 + "\tHeterosexual Infection\t" + genotype +"\t"+ agent2 +"\t"+ turn;
            }
        }else if(infectMode == INFECT_MOTHERTOCHILD){
            yearPrevalence++;
            log = agent1 + "\tM2C Infection\t" + genotype +"\t"+ agent2 +"\t"+ turn;
        }
        eventQueue.add(log);
    }
    public void insertProgression(int agent, int stage){
        if(logLevel < LOG_PROGRESSION) return;
        String log = agent + "\tProgression\t" + stage + "\t\t" + turn;
        eventQueue.add(log);
    }
    public void insertConception(int agent1, int agent2){
        if(logLevel < LOG_CONCEPTION) return;
        String log = agent1 + "\tConceived\t\t" +agent2 + "\t" + turn; 
        eventQueue.add(log);
    }
    public void insertBirth(Agent a, boolean CCR5Homo){
        livingAgents++;
        yearBirth++;
        if(CCR5Homo) CCR5Homozygous++;
        if(logLevel < LOG_BIRTH) return;
        String log = a.ID + "\tBorn\t\t\t" + turn; 
        eventQueue.add(log);
        log = a.ID + "\t";
        if(a.isFemale()){
            log = log + "F\t";
        }else{
            log = log + "M\t";
        }
        log = log + a.getFaithfulness() + "\t" + a.getWantLevel() + "\t" + a.getCondomUse() + "\t" + a.getCCR5Resistance() + "\t" + a.getImmunityFactors();
        try{
            agentOut.newLine();
            agentOut.write(log);
        }catch(IOException e){
            System.err.println("Could not print to agent: " + e.getLocalizedMessage() + "\n" + log);
        }
    }
    public void insertDeath(int agent1, boolean natural, boolean infected, boolean CCR5Homo){
        yearDeath++;
        livingAgents--;
        if(CCR5Homo) CCR5Homozygous--;
        if(logLevel < LOG_DEATH) return;
        String log = agent1 + "\t";
        if(natural){
            if(infected){
                yearPrevalence--;
                log = log + "Infected ";
            }
            log = log + "Non-AIDS Death\t\t\t" + turn;
        }else{
            yearMortality++;
            yearPrevalence--;
            log = log + "AIDS Death\t\t\t" + turn;
        }
        eventQueue.add(log);
    }
    public void firstSet(Agent[] agents){// for setting up that initial number of homozygous CCR5Delta32 mutations. 
        for(Agent a : agents){
            if(a.getCCR5Resistance() == 100) CCR5Homozygous++;
            if(logLevel < LOG_INFECT)continue;
            String log = a.ID + "\t";
            if(a.isFemale()){
                log = log + "F\t";
            }else{
                log = log + "M\t";
            }
            
            log = log + a.getFaithfulness() + "\t" + a.getWantLevel() + "\t" + a.getCondomUse() + "\t" + a.getCCR5Resistance() + "\t" + a.getImmunityFactors();
            try{
                agentOut.newLine();
                agentOut.write(log);
            }catch(IOException e){
                System.err.println("Could not print to agent: " + e.getLocalizedMessage() + "\n" + log);
            }
        }
    }
    
    
    public HIVLogger(int level, String event, String year, String agent, int numAgents, int numInfect) throws IOException{
        logLevel = level;
        yearPrevalence = numInfect;
        livingAgents = yearLiving= numAgents;
        
        yearOut = new BufferedWriter(new FileWriter(year, false),(8*1024)); // second argument F means will overwrite if exists. 
        yearOut.write("Year\tStarting Population\tIncidence\tPrevelance\tMortality\tBirth Rate\tDeath Rate\tCCR5 Homozygous");
        
        eventOut = new BufferedWriter(new FileWriter(event, false),(8*1024)); // second argument F means will overwrite if exists. 
        eventOut.write("Agent\tAction\tGenoType/Stage\tAgent\tStep");
        
        agentOut = new BufferedWriter(new FileWriter(agent, false),(8*1024)); // second argument F means will overwrite if exists. 
        agentOut.write("ID\tGender\tFaithfulness\tWant\tCondom Usage\tCCR5\tImmune Factors");
        
        eventQueue = new ArrayDeque();
    }
    public HIVLogger(){
        //assuming you don't want to log anything. 
        logLevel = -1;
        eventOut = null;
        yearOut = null;
        eventQueue = null;
        agentOut = null;
    }
    
    
    
}

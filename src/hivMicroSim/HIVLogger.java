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
    
    

    //log levels.
    public final static int LOG_ALL = 100;
    public final static int LOG_NONE = -1; // do not log.
    public final static int LOG_YEARLY = 0; //logs yearly amounts, but nothing relating to the individual agents
    public final static int LOG_INFECT = 10; // last level that logs agents.
    public final static int LOG_DISCOVERY = 15;
    public final static int LOG_PROGRESSION = 20;
    public final static int LOG_DEATH = 30;
    public final static int LOG_DEATH_NATURAL = 35;
    public final static int LOG_ENTRY = 40;
    
    private int year = 0;
    private int month = 0; // month 0 (first call to step) will be the initial pre simulation values
    private int turn = 0;
    //keeping yearly tallies since all these are reported and it
    //means we don't have to query all agents
    private int yearGrowth = 0; // the number of new agents added.
    private int yearDeath = 0; //the total number of agents that died of any cause
    private int yearMortality = 0; // the number of agents that died of AIDs
    private int yearInfect = 0;  //The number of agents infected that year.
    private int prevalence = 0; //The number of agents living with the disease that year.
    private int yearLiving = 0; //the number of agents alive at the start of the year for rate calculations;
    private int livingAgents = 0;
    
    //note that 
    
    
    @Override
    public void step(SimState state){
        if(logLevel == LOG_NONE)return;   
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
            String log = year + "\t"+ yearLiving + "\t" + yearInfect + "\t" + prevalence + "\t" 
                    + yearMortality + "\t" + yearGrowth + "\t" + yearDeath;
            try{
                yearOut.newLine();
                yearOut.write(log);
            }catch(IOException e){
                System.err.println("Could not write yearly report for year: " + year + " "+e.getLocalizedMessage()+"\n" + log);
            }
            yearLiving = livingAgents;//the number of agents at the start of the year
            yearGrowth = yearDeath=yearMortality=yearInfect=0; //reset the yearly values
            year++;
            month = 1;
        }
    }
    
    public void close(){
        if(logLevel == LOG_NONE)return;
        try{
            yearOut.flush();
            eventOut.flush();
            agentOut.flush();
            yearOut.close();
            eventOut.close();
            agentOut.close();
        }catch(IOException e){
            System.err.println("Could not close log file! " + e.getLocalizedMessage());
        }
    }
    
    public void insertInfection(int infectMode, int infector, int infected, int commitmentLevel, int attemptsToInfect){
        yearInfect++;
        prevalence++;
        if(logLevel < LOG_INFECT) return;
        String log = turn + "\t" + infector +"\t" ;
        switch(infectMode){
            case Agent.MODEAI:
                log = log + "Anal Insertive";
                break;
            case Agent.MODEAR:
                log = log + "Anal Receptive";
                break;
            case Agent.MODEVR:
                log = log + "Vaginal Receptive";
                break;
            case Agent.MODEVI:
                log = log + "Vaginal Insertive";
                break;
        }
        log = log + "\t" + infected + "\t" + commitmentLevel + "\t" + attemptsToInfect;
        eventQueue.add(log);
    }
    
    public void insertProgression(int agent, int stage, int ticks){
        if(logLevel < LOG_PROGRESSION) return;
        String log = turn + "\t" + agent + "\tProgression\t" + stage + "\t" + ticks + "\t";
        eventQueue.add(log);
    }
    public void insertDiscovery(int agent, int stage, int ticks){
        if(logLevel < LOG_DISCOVERY) return;
        String log = turn + "\t" + agent + "\tDiscovery\t" + stage + "\t" + ticks + "\t";
        eventQueue.add(log);
    }
    private void logNewAgent(Agent a){
        //this is really just so that the initial dump can use THIS method and we don't have to maintain 2 logs inserting
        //new agents... there was already a fun little bug where changes to insertNewAgent weren't carried over into the
        //initial dump.
        if(logLevel < LOG_INFECT) return;
        String log2 = turn +"\t" + a.ID + "\t";
        if(a.isFemale()){
            log2 = log2 + "F\tNA\tNA\t";
        }else{
            hivMicroSim.Agent.Male m = (hivMicroSim.Agent.Male)a;
            log2 = log2 + "M\t" + m.getMSW() + "\t" + m.getMSM() + "\t";
        }
        log2 = log2 + a.getCommitment() + "\t" + a.getMonogamous() + "\t"+ a.getLibido() + "\t" + a.getCondomUse() + "\t" + 
                a.hivImmunity;
        try{
            agentOut.newLine();
            agentOut.write(log2);
        }catch(IOException e){
            System.err.println("Could not print to agent: " + e.getLocalizedMessage() + "\n" + log2);
        }
        if(logLevel < LOG_ENTRY) return;
        String log = turn +"\t" + a.ID + "\tEntered\t" + a.getAge(); 
        eventQueue.add(log);
    }
    public void insertNewAgent(Agent a){
        livingAgents++;
        yearGrowth++;
        logNewAgent(a);
    }
    
    public void insertDeath(int agent, boolean natural, boolean infected, int ticks){//ticks is only used in the event of AIDS death
        yearDeath++;
        livingAgents--;
        if(infected) {
            prevalence--;
            if(!natural){
                yearMortality++;
            }
        }
        if((logLevel < LOG_DEATH_NATURAL && natural) || (logLevel< LOG_DEATH && !natural)) return;
        String log = turn + "\t"+ agent + "\t";
        if(natural){
            if(infected){
                log = log + "Infected ";
            }
            log = log + "Non-AIDS Death\t" ;
        }else{
            log = log + "AIDS Death\t" + ticks;
        }
        log = log + "\t\t";
        eventQueue.add(log);
    }
    
    public void firstSet(sim.util.Bag agents){
        //log the initial bag.
        if(logLevel < LOG_INFECT)return; // last level that records agents
        agents.stream().forEach((o) -> {
            logNewAgent((Agent)o);
        });
    }
    
    public HIVLogger(int level, String event, String year, String agent, int numAgents, int numInfect) throws IOException{
        logLevel = level;
        prevalence = numInfect;
        livingAgents = yearLiving = numAgents;
        
        yearOut = new BufferedWriter(new FileWriter(year, false),(8*1024)); // second argument F means will overwrite if exists. 
        yearOut.write("Year\tStarting.Population\tIncidence\tPrevelance\tMortality\tGrowth\tDeath.Rate");
        
        eventOut = new BufferedWriter(new FileWriter(event, false),(8*1024)); // second argument F means will overwrite if exists. 
        eventOut.write("Tick\tAgent\tAction\tDesc1(StageAgeAgent)\tDesc2(TicksCommitmentLevel)\tDesc3(AtteptsToInfect)");
        
        agentOut = new BufferedWriter(new FileWriter(agent, false),(8*1024)); // second argument F means will overwrite if exists. 
        agentOut.write("Entry.Step\tID\tGender\tMSW\tMSM\tCommitment\tMonogamous\tLibido\tCondom.Usage\tImmunity");
        
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

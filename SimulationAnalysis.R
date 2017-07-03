library(ggplot2)
library(reshape2)
library(car)#recode
library(sqldf)

agentLog = read.table("agentLog.txt", header = TRUE, sep = "\t")
eventLog = read.table("eventLog.txt", header = TRUE, sep = "\t")
yearLog = read.table("yearLog.txt", header = TRUE, sep = "\t")
yearLog$percentInfected = yearLog$Prevelance/yearLog$Starting.Population * 100
yearLog$incidenceRate = yearLog$Incidence/yearLog$Starting.Population * 1000

vaginalInfect = eventLog[grep("Vaginal", eventLog$Action),]
analInfect = eventLog[grep("Anal", eventLog$Action),]
infect = rbind(vaginalInfect, analInfect)
rNot = table(infect$Agent)

highR0 = as.data.frame(rNot[rNot>2])
iAgents = agentLog[agentLog$ID%in%highR0$Var1,]
#changing this to grep for 'sexual' to avoid mother to child infections.
infAgents = agentLog[agentLog$ID %in% infect[,"Desc1.StageAgeAgent."],]
nonInfAgents = agentLog[!agentLog$ID %in% infect[,"Desc1.StageAgeAgent."],]
popGrowthRate = log(yearLog[nrow(yearLog), "Starting.Population"]/yearLog[1,"Starting.Population"])/nrow(yearLog)
infectPattern = sqldf(
  'select inf.*, e.infector, ePA.toAIDs, ePD.toDeath, disc.toDiscovery, rNot.infected, duration.Infection_Duration
  from infAgents inf
  left join (select "Desc1.StageAgeAgent." infected, Agent infector from eventLog where Action like "Vaginal%" or Action like "Anal%") e on inf.ID == e.infected
  left join (select Agent, "Desc2.Ticks." toAIDs from eventLog where Action == "Progression" and "Desc1.StageAgeAgent." == 3) ePA on inf.ID == ePA.Agent
  left join (select Agent, "Desc1.StageAgeAgent." toDeath from eventLog where Action == "AIDS Death") ePD on inf.ID == ePD.Agent
  left join (select Agent, "Desc2.Ticks." toDiscovery from eventLog where Action == "Discovery") disc on inf.ID == disc.Agent
  left join (select Agent, count(*) infected from infect group by Agent) rNot on inf.ID == rNot.Agent 
  left join (select e.Agent Agent, (e.Tick - ei.Tick) Infection_Duration 
  from eventLog e 
  join (select Tick, "Desc1.StageAgeAgent." Agent 
  from eventLog
  where Action in ("Vaginal Insertive","Vaginal Receptive","Anal Insertive","Anal Receptive")
  ) ei on e.Agent = ei.Agent
  where e.Action in ("AIDS Death", "Infected Non-AIDS Death")) duration on duration.Agent = inf.ID
  ')
#For some reason the number of people infected is showing up as a character sometimes. 
infectPattern$infected = as.integer(infectPattern$infected)
infectPattern$Infection_Duration = as.numeric(infectPattern$Infection_Duration)
infectPattern$infected[is.na(infectPattern$infected)] = 0
infectPattern$InfPerYear = infectPattern$infected/(infectPattern$Infection_Duration/52)

#Going to add MSM MSWO and W to the year log.

plot(yearLog$Year, yearLog$incidenceRate, type = "l", main = "Incidicent rate per 1,000 agents")
plot(yearLog$Year, yearLog$percentInfected, type = "l", main = "Prevalence in Agent Population")
plot(yearLog$Starting.Population, type = "l", main = "Model Population")

#Multivariable plots
tmp = yearLog[, c("Year","Starting.Population","Prevelance")];melted = melt(tmp, id = "Year");ggplot(data = melted, aes(x = Year, y = value, color = variable)) + geom_line() 
tmp = yearLog[, c("Year","percentInfected","incidenceRate")];tmp$mortalityRate = (yearLog$Mortality/yearLog$Starting.Population * 1000);melted = melt(tmp, id = "Year");ggplot(data = melted, aes(x = Year, y = value, color = variable)) + geom_line() + ggtitle("Prevalence, Incidence, and Mortality\n Even Libido")

#Rnot and t-test- Compare population profiles for non-infected, infected, and high Rnot

paste("Mean rNot (in those that did infect others):", mean(rNot))
paste("Max rNot:", max(rNot))

summary(agentLog)
summary(nonInfAgents)
summary(infAgents)
summary(iAgents)

#all infected vs non infected agents 
t.test(infAgents$Commitment, nonInfAgents$Commitment)
t.test(infAgents$Monogamous, nonInfAgents$Monogamous)
t.test(infAgents$Libido, nonInfAgents$Libido)
t.test(infAgents$Condom.Usage, nonInfAgents$Condom.Usage)
t.test(infAgents$Immunity, nonInfAgents$Immunity)


#high infectors And Non Infected
t.test(iAgents$Commitment, nonInfAgents$Commitment)
t.test(iAgents$Monogamous, nonInfAgents$Monogamous)
t.test(iAgents$Libido, nonInfAgents$Libido)
t.test(iAgents$Condom.Usage, nonInfAgents$Condom.Usage)
t.test(iAgents$Immunity, nonInfAgents$Immunity)

#t.test(agentLog$Selectivity, iAgents$Selectivity)
#infected agents (minus initial infected who were randomly selected) and high infectors
t.test(iAgents$Commitment, infAgents$Commitment)
t.test(iAgents$Monogamous, infAgents$Monogamous)
t.test(iAgents$Libido, infAgents$Libido)
t.test(iAgents$Condom.Usage, infAgents$Condom.Usage)
t.test(iAgents$Immunity, infAgents$Immunity)

########Looking at the duration of infection#####
#build the infection data set

if(length(which(!is.na(infectPattern$toAIDs))) != 0){ paste("Years to AIDS");round((quantile(infectPattern$toAIDs, na.rm = TRUE) + 2)/52, 2)}else paste("No AIDs")
if(length(which(!is.na(infectPattern$toDeath))) != 0){ paste("Years to AIDS Death"); round((quantile(infectPattern$toAIDs + infectPattern$toDeath, na.rm = TRUE)+2)/52,2)}else paste("No AIDs Deaths")
if(length(which(!is.na(infectPattern$toDeath))) != 0){ paste("Years to Death from AIDs"); round(quantile(infectPattern$toDeath, na.rm = TRUE)/52,2)} else paste ("No AIDs Deaths")
if(length(which(!is.na(infectPattern$toDiscovery))) != 0){paste("Years to discovery"); round(quantile(infectPattern$toDiscovery, na.rm = TRUE)/52,2)}else {paste("No Discovery")}
paste("Overall survival (Years)"); round(quantile(infectPattern$Infection_Duration, na.rm = TRUE)/52,2)
paste("Mean infections per year infected: ", round(mean(infectPattern$InfPerYear, na.rm = TRUE), 2))
paste("Expected infections per infected individual: ",  round(mean(infectPattern$InfPerYear, na.rm = TRUE) * (mean(infectPattern$Infection_Duration, na.rm = TRUE)/52),2))
#Is knowledge power? Split up pre and post discovery infections 


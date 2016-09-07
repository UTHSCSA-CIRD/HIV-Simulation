library(ggplot2)
library(reshape2)
library(car)#recode
library(sqldf)
agentLog = read.table("agentLog.txt", header = TRUE, sep = "\t")
eventLog = read.table("eventLog.txt", header = TRUE, sep = "\t")
yearLog = read.table("yearLog.txt", header = TRUE, sep = "\t")
vaginalInfect = eventLog[grep("Vaginal", eventLog$Action),]
analInfect = eventLog[grep("Anal", eventLog$Action),]
infect = rbind(vaginalInfect, analInfect)
rNot = table(infect$Agent)
yearLog$percentInfected = yearLog$Prevelance/yearLog$Starting.Population * 100
yearLog$incidenceRate = yearLog$Incidence/yearLog$Starting.Population * 1000
highR0 = as.data.frame(rNot[rNot>2])
iAgents = agentLog[agentLog$ID%in%highR0$Var1,]
#changing this to grep for 'sexual' to avoid mother to child infections.
infAgents = agentLog[agentLog$ID %in% infect[,"Desc1.StageAgeAgent."],]
popGrowthRate = log(yearLog[nrow(yearLog), "Starting.Population"]/yearLog[1,"Starting.Population"])/nrow(yearLog)
infectPattern = sqldf(
  'select inf.*, e.infector, ePA.toAIDs, ePD.toDeath, disc.toDiscovery, rNot.infected, duration.Infection_Duration
  from infAgents inf
  left join (select "Desc1.StageAgeAgent." infected, Agent infector from eventLog where Action like "Vaginal%" or Action like "Anal%") e on inf.ID == e.infected
  left join (select Agent, "Desc2.TicksCommitmentLevel." toAIDs from eventLog where Action == "Progression" and "Desc1.StageAgeAgent." == 3) ePA on inf.ID == ePA.Agent
  left join (select Agent, "Desc1.StageAgeAgent." toDeath from eventLog where Action == "AIDS Death") ePD on inf.ID == ePD.Agent
  left join (select Agent, "Desc2.TicksCommitmentLevel." toDiscovery from eventLog where Action == "Discovery") disc on inf.ID == disc.Agent
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
infectPattern$infected[is.na(infectPattern$infected)] = 0
infectPattern$InfPerYear = infectPattern$infected/(infectPattern$Infection_Duration/52)


plot(yearLog$Year, yearLog$incidenceRate, type = "l")
plot(yearLog$Year, yearLog$percentInfected, type = "l")
plot(yearLog$Starting.Population, type = "l")

#Multivariable plots
tmp = yearLog[, c("Year","Starting.Population","Prevelance")];melted = melt(tmp, id = "Year");ggplot(data = melted, aes(x = Year, y = value, color = variable)) + geom_line() 
tmp = yearLog[, c("Year","percentInfected","incidenceRate")];tmp$mortalityRate = (yearLog$Mortality/yearLog$Starting.Population * 1000);melted = melt(tmp, id = "Year");ggplot(data = melted, aes(x = Year, y = value, color = variable)) + geom_line()

#Rnot and t-test- Compare population profiles for non-infected, infected, and high Rnot

paste("Mean rNot (in those that did infect others):", mean(rNot))
paste("Max rNot:", max(rNot))

summary(agentLog)
summary(infAgents)
summary(iAgents)

#all infected vs non infected 
t.test(infAgents$Commitment, agentLog$Commitment)
t.test(infAgents$Monogamous, agentLog$Monogamous)
t.test(infAgents$Libido, agentLog$Libido)
t.test(infAgents$Condom.Usage, agentLog$Condom.Usage)
t.test(infAgents$Immunity, agentLog$Immunity)

#t.test(infAgents$Selectivity, agentLog$Selectivity)
#all agents and high infectors
t.test(iAgents$Commitment, agentLog$Commitment)
t.test(iAgents$Monogamous, agentLog$Monogamous)
t.test(iAgents$Libido, agentLog$Libido)
t.test(iAgents$Condom.Usage, agentLog$Condom.Usage)
t.test(iAgents$Immunity, agentLog$Immunity)

#t.test(agentLog$Selectivity, iAgents$Selectivity)
#infected agents (minus initial infected who were randomly selected) and high infectors
t.test(iAgents$Commitment, infAgents$Commitment)
t.test(iAgents$Monogamous, infAgents$Monogamous)
t.test(iAgents$Libido, infAgents$Libido)
t.test(iAgents$Condom.Usage, infAgents$Condom.Usage)
t.test(iAgents$Immunity, infAgents$Immunity)

########Looking at the duration of infection#####
#build the infection data set

paste("Mean years to AIDS:", (mean(infectPattern$toAIDs, na.rm = TRUE)+2)/52)
paste("Mean years to AIDS Death:", (mean(infectPattern$toAIDs + infectPattern$toDeath, na.rm = TRUE)+2)/52)
paste("Mean years to Death from AIDs:", (mean(infectPattern$toDeath, na.rm = TRUE))/52)
paste("Mean time to discovery: ", (mean(infectPattern$toDiscovery, na.rm = TRUE)/52))
paste("Mean overall survival: ", (mean(infectPattern$Infection_Duration, na.rm = TRUE)/52))
paste("Mean infections per year infected: ", mean(infectPattern$InfPerYear, na.rm = TRUE))
paste("Expected infections per infected individual: ", mean(infectPattern$InfPerYear, na.rm = TRUE) * (mean(infectPattern$Infection_Duration, na.rm = TRUE)/52))
#Is knowledge power? Split up pre and post discovery infections 


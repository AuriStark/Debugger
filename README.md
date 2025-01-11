# Debugger
 # Part 4: Debugger Implementation - Part II

Goals:

* Extend the debugger with at least two optional use cases
* Write a user-facing documentation which explains how to use the debugger (at
  most 3 pages)
* Write a technical documentation which explains in detail how the debugger
  (backend, communication with frontend) works (at most 4 pages)


Meetings are now done on demand and per group.
Please request a meeting at least one day in advance.

Deadline: **21.2.2022** (extended by one week!)


# Part 3: Debugger Implementation

Goals:

* Implement a debugger that is able to debug the **mandatory** use cases.

You can make use of and implement any of these interfaces to inject your debugger behavior:

* `Addon`: Executed **before** any workflow specification is loaded. Has access
  to program arguments and class dependencies.
* `Hook`: Executed **after** workflow specifications are parsed and **before**
  workflows are executed. Has access to program arguments, class dependencies,
  and workflow specification and (parsed) workflow instances.
* `NodeHook`: Executed **before** or **after** a node is starting or has
  finished processing a flow. Has access to the node container and the flow map.


Meetings are now done on demand and per group.
Please request a meeting at least one day in advance.

First draft deadline: 10.1.2022

# Part 2: Preparations

Goals:

* Look at examples provided
  * Front end skeleton for JavaFX
  * Front end prototype for pure HTML/JS
  * Back-end debugger plugin for old ScraperFlow version

* Look at workflow debugger use cases

* Decide what language and framework to use for the front end
* Plan group responsibilities, front-end <-> backend


Next meeting: **2.12.2021**


# Part 1: ScraperFlow Introduction & Websockets

Goals:

* Familiarize yourself with [Websockets](https://www.html5rocks.com/en/tutorials/websockets/basics/https://www.html5rocks.com/en/tutorials/websockets/basics/)
* Play around with [ScraperFlow](https://github.com/scraperflow/scraperflow)

Until: **18.11.2021**

## Task 1: Compile ScraperFlow

Clone the Github repository of
[ScraperFlow](https://github.com/scraperflow/scraperflow) and build the project
by following the instructions in the Readme. Only a recent Java version is
needed.

## Task 2: Word Count


Implement a workflow `wordcount.yml` which counts all words in a folder `data`,
which is expected to be located in the same folder as the `wordcount.yml` file.


Implement only one custom runetime node for word count (e.g. `WordCount.java`).
Otherwise use only the [library nodes](https://docs.scraperflow.server1.link) to
define the workflow.

After execution of the workflow, the console output should be one line per word,
e.g.

```
my 120
word 1
count 1
```


Hints:

* You can use [Map](https://scraperflow.server1.link/?q=docs#Map) and
  [JoinSingle](https://scraperflow.server1.link/?q=docs#JoinSingle) to join
  multiple concurrent flows

## Task 3: WebSockets

Familiarize yourself with [Websockets](https://www.html5rocks.com/en/tutorials/websockets/basics/https://www.html5rocks.com/en/tutorials/websockets/basics/),
as this will be the connection between the debugger backend and frontend.

## Task 4: Frontend

Discuss among your group your preferred choice for the debugger frontend.

Some things to consider:

* It should have WebSockets support
* It should be cross-platform (Linux, Windows, Apple support)
* It should support JSON object processing


Some possible choices:

* Pure javascript
* WebComponents
* JavaFx or Swing
* Angular/React/Vue/...
* ...



## Links

* https://github.com/scraperflow/scraperflow
* https://github.com/scraperflow/scraperflow/wiki
* [Node Documentation](https://docs.scraperflow.server1.link)

cc: https://github.com/scraperflow/scraperflow

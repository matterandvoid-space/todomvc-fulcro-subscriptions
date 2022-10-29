TodoMVC [https://todomvc.com](https://todomvc.com) full stack sample application.

The main purpose of this repository is to demonstrate how to use fulcro purely as a data management and transaction management library.
Subscriptions on top of the fulcro app db with rendering performed by a pure UI library (helix) provide great separation of concerns.

Additionally the repository can serve as an example working full stack clojure application for general learning purposes, 
both for the author and the reader.

Tech stack:

- Fulcro https://github.com/fulcrologic/fulcro - only used for data maniuplation, managing state, executing transactions
  - use fulcro inspect https://github.com/dvingo/fulcro-inspect to debug app state, EQL queries, and transactions.
- helix https://github.com/lilactown/helix - UI rendering layer interface with React
- subscriptions https://github.com/matterandvoid-space/subscriptions - reactive subscriptions on top of fulcro DB, integration with React hooks
- react-hook-form https://github.com/react-hook-form/react-hook-form - manage state and callbacks for UI form inputs
- react-router v6 https://github.com/remix-run/react-router - URL management
- malli https://github.com/metosin/malli - validate data against schema on client and server, instrument functions during development
- datalevin https://github.com/juji-io/datalevin - datalog database
- Pathom3 https://github.com/wilkerlucio/pathom3 - resolvers and mutations over the DB
- ring-undertow-adapter https://github.com/luminus-framework/ring-undertow-adapter - webserver
- muuntaja https://github.com/metosin/muuntaja - content negotiation, transit, json, etc
- reitit https://github.com/metosin/reitit - backend routing
- nexus https://github.com/nivekuil/nexus - lifecycle component management built on top of pathom3
- babashka tasks https://babashka.org - for automation

I have been exploring how to integrate re-frame with fulcro for some time. The subscriptions library is the current iteration
of this exploration.

I was fighting fulcro at the UI level - struggling to figure out how to construct queries for each piece of UI, things
became especially pernicious for denormoralized data. After some time it dawned on me that fulcro components are wonderful
for declaring normalized domain data, but terrible for data manipulation on top of that data. Needing to construct artificial
`idents` - a tool specifically designed for auto-normalization - just to manipulate data into the correct shape hit me on the 
head one day as an obvious indication that this is the wrong tool for the job. 
re-frame subscriptions are perfect for this job and thus the subscriptions library was born.

This repo started out as a proof of concept for integrating all of the above libraries together into a cohesive tech stack, but 
figured other people may find it useful as well.

# Usage

main prerequisites:

- java https://www.azul.com/downloads/?package=jdk
- node.js https://nodejs.org/en/download/
- yarn https://classic.yarnpkg.com/lang/en/
- babashka https://babashka.org/

Compile the frontend code:
```bash
bb fe
```

If you have any favorite dev-time aliases define in `~/.clojure/deps.edn` you can add them as an argument:
```bash
bb fe :my-dev:scope-capture
```
You will see the resulting commandline invocation to shadow-cljs in the terminal output.

Or edit the top block in `bb.edn` to pass them in by default.

---

After that compiles then start the backend.

Start a Clojure REPL via your favorite editor with the aliase `:dev`.
In the `src/dev/user.clj` file there is a comment block to start and stop the server.

Other useful tasks:

```bash
bb release
```
Builds the frontend release and the backend uberjar.

```bash
bb run-jar
```
starts the production app using java by default on port 8499.

Run:
```bash
bb tasks
```
To see all of the available tasks.

# Notes

The clojurescript build includes some no-op namespaces to eliminate as much dev-time only code as possible from the release
build including a fork of fulcro that cuts out rendering code from the default fulcro application constructor:

https://github.com/dvingo/fulcro/tree/no-ui.

The upstream fulcro release build is about 100kb of optimized JS, this version is ~80kb, which is still the 3rd largest
dependency after clojurescript and react-dom.

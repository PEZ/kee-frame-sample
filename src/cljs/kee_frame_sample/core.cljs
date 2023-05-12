(ns kee-frame-sample.core
  (:require [cljs.spec.alpha :as s]
            [cljsjs.material-ui]
            [kee-frame-sample.controller.common]
            [kee-frame-sample.controller.league]
            [kee-frame-sample.controller.leagues]
            [kee-frame-sample.controller.live]
            [kee-frame-sample.layout :as layout]
            [kee-frame-sample.subscriptions]
            [kee-frame-sample.view.form :as form]
            [kee-frame-sample.view.league :as league]
            [kee-frame-sample.view.live :as live]
            [kee-frame.core :as k]
            [kee-frame.error :as error]
            [kee-frame.event-logger :as event-logger]
            [re-frame.core :as f :refer [subscribe]]
            [taoensso.timbre :as t-log]))

(comment
  t-log/*config*
  (t-log/info "foo")
  (t-log/debug "foo")
  (println "foo")
  :rcf)

(goog-define debug false)

(enable-console-print!)

(defn error-body [[err info]]
  (js/console.log "An error occurred: " info)
  (js/console.log "Context: " err)
  [:div "Something went wrong"])

(defn dispatch-main []
  [error/boundary
   error-body
   [k/case-route (comp :name :data)
    :league [league/league-dispatch]
    :live [live/live]
    :form [form/form]
    [:div "Loading..."]]])

(def routes [["/" :live]
             ["/form" :form]
             ["/league/:id/:tab" :league]])

(def initial-db {:drawer-open?  false
                 :leagues       nil
                 :fixtures      nil
                 :table         nil
                 :live-matches  nil
                 :ongoing-only? false})

(f/reg-event-fx :route-changed
  (fn [_ [_ route]]
    ;; Do your custom stuff here. Like making the next line conditional to skip triggering controllers
    {:dispatch [:kee-frame.router/route-changed route]}))

(s/def ::league (s/keys :req-un [::name ::id]))
(s/def ::leagues (s/nilable (s/coll-of ::league)))
(s/def ::db-spec (s/keys :req-un [::drawer-open? ::leagues ::fixtures ::table ::live-matches ::ongoing-only?]))

(defn render! []
  (k/start! {:log                 {:min-level        :debug
                                   :ns-blacklist ["kee-frame.event-logger"]}
             :route-change-event  :route-changed
             :global-interceptors [event-logger/interceptor]
             :not-found           "/"
             :screen              true
             :scroll              false
             :routes              routes
             :hash-routing?       false
             :initial-db          initial-db
             :root-component      [layout/main-panel [dispatch-main]]
             :app-db-spec         ::db-spec}))

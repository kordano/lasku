(ns lasku.core
  (:require-macros [secretary.core :refer [defroute]])
  (:import goog.history.Html5History)
  (:require [secretary.core :as secretary]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [reagent.core :as r]))

(enable-console-print!)

(defn hook-browser-navigation! []
  (doto (Html5History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

(defn app-routes [state]
  (secretary/set-config! :prefix "#")

  (defroute "/" []
    (swap! state assoc :page :home))

  (defroute "/customers" []
    (swap! state assoc :page :customers))

  (defroute "/customers/:cid/projects/:pid" [cid pid]
    (swap! state assoc :selected-customer cid)
    (swap! state assoc :selected-project pid)
    (swap! state assoc :page :project))

  (hook-browser-navigation!))

(defonce app-state (r/atom {:selected-customer 0
                            :user/name         "Konrad"
                            :user/email        "konrad@lambdaforge.io"
                            :user/customers    [{:customer/department "Department of floating bananas"
                                                 :customer/name       "Institute of Awesomeness"
                                                 :customer/email      "yolo@awesome.ness"
                                                 :customer/contact    "Yolo Yolo"
                                                 :customer/street     "Mainstreet 666"
                                                 :customer/zip        "445522"
                                                 :customer/city       "Heaven"
                                                 :customer/country    "Germany"
                                                 :customer/id         "1"
                                                 :customer/projects   [{:project/name                 "I am awesome"
                                                                        :project/advisor              "Konrad Kühne"
                                                                        :project/ref                  "20181031-01"
                                                                        :project/default-effort-price 100
                                                                        :project/default-price-unit   :euro
                                                                        :project/id                   "3"
                                                                        :project/task-groups
                                                                        [{:task-group/name  "Android"
                                                                          :task-group/id    "4"
                                                                          :task-group/tasks [{:task/name         "API Design"
                                                                                              :task/effort       8
                                                                                              :task/id           "5"
                                                                                              :task/effort-unit  :hour
                                                                                              :task/effort-price 120
                                                                                              :task/price-unit   :euro}
                                                                                             {:task/name         "Login Screen"
                                                                                              :task/id           "6"
                                                                                              :task/effort       3
                                                                                              :task/effort-unit  :hour
                                                                                              :task/effort-price 100
                                                                                              :task/price-unit   :euro}
                                                                                             {:task/name         "Profile Screen"
                                                                                              :task/id           "11"
                                                                                              :task/effort       5
                                                                                              :task/effort-unit  :hour
                                                                                              :task/effort-price 100
                                                                                              :task/price-unit   :euro}
                                                                                             ]}
                                                                         {:task-group/name  "iOS"
                                                                          :task-group/id    "10"
                                                                          :task-group/tasks [{:task/name         "Login Screen"
                                                                                              :task/id           "6"
                                                                                              :task/effort       3
                                                                                              :task/effort-unit  :hour
                                                                                              :task/effort-price 100
                                                                                              :task/price-unit   :euro}
                                                                                             {:task/name         "Profile Screen"
                                                                                              :task/id           "12"
                                                                                              :task/effort       5
                                                                                              :task/effort-unit  :hour
                                                                                              :task/effort-price 100
                                                                                              :task/price-unit   :euro}]}]
                                                                        }]}
                                                {:customer/department "HR"
                                                 :customer/name       "Your Big Corporate Ltd."
                                                 :customer/contact    "Fancy Shmancy"
                                                 :customer/id         "2"
                                                 :customer/email      "fancy@shmancy"
                                                 :customer/street     "Grey Alley"
                                                 :customer/zip        "12345"
                                                 :customer/city       "Corporate City"
                                                 :customer/country    "Germany"
                                                 :customer/projects   [{:project/name                 "Evil Incorporated 2"
                                                                        :project/advisor              "Konrad Kühne"
                                                                        :project/ref                  "20181031-02"
                                                                        :project/default-effort-price 500
                                                                        :project/default-price-unit   :euro
                                                                        :project/id                   "7"
                                                                        :project/task-groups
                                                                        [{:task-group/name  "Management"
                                                                          :task-group/id    "8"
                                                                          :task-group/tasks [{:task/name         "7.7.2018"
                                                                                              :task/effort       8
                                                                                              :task/id           "9"
                                                                                              :task/effort-unit  :hour
                                                                                              :task/effort-price 500
                                                                                              :task/price-unit   :euro}
                                                                                             {:task/name         "14.7.2018"
                                                                                              :task/effort       4
                                                                                              :task/id           "10"
                                                                                              :task/effort-unit  :hour
                                                                                              :task/effort-price 500
                                                                                              :task/price-unit   :euro}]}]}]}]}))


(defn task-component [{:keys [:task/name :task/effort :task/price-unit :task/effort-price :task/effort-unit]}]
  [:tr
   [:td name]
   [:td effort-unit]
   [:td price-unit]
   [:td effort-price]
   [:td effort]
   [:td (* effort effort-price)]])

(defn task-group-component [{:keys [:task-group/name :task-group/tasks]}]
  [:div.container.mt-10
   [:h3 name]
   [:table.table.table-striped
    [:thead
     [:tr
      [:th "Name"]
      [:th "Effort Unit"]
      [:th "Price Unit"]
      [:th "Price per unit"]
      [:th "Effort"]
      [:th "Price"]]]
    [:tbody
     (for [task tasks]
       [task-component task])
     [:br]
     [:tr
      [:td "Sum"]
      [:td ]
      [:td]
      [:td]
      [:td (reduce + (map :task/effort tasks))]
      [:td (reduce + (map #(* (:task/effort %) (:task/effort-price %)) tasks))]]]]])

(defn project-component [state]
  (let [{:keys [selected-project selected-customer :user/customers]}                                 @state
        {:keys [:project/name :project/advisor :project/ref :project/task-groups]} (->> customers
                                                                                        (filter #(= (:customer/id %) selected-customer))
                                                                                        first
                                                                                        :customer/projects
                                                                                        (filter #(= (:project/id %) selected-project))
                                                                                        first)]
    [:div.container
     [:h2 name [:small.text-gray.float-right ref]]
     [:p advisor]
     (for [task-group task-groups]
       [task-group-component task-group])
     ]))

(defn nav-component [state]
  [:header
   [:section.navbar-section
    [:a.navbar-brand.mr-2 {:href "#/"} "lasku"]
    [:a.btn.btn-link {:href "#/customers"} "customers"]]])

(defn customer-component [state]
  [:div
   [:h3 "Projects"]
   (for [project (:customer/projects ((:user/customers @state) (:selected-customer @state)))]
     [project-component project])])

(defn customer-profile-tile [title subtitle]
  [:div.tile.tile-centered
   [:div.tile-content
    [:div.tile-title.text-bold title]
    [:div.tile-subtitle subtitle]]])

(defn customer-project-tile [{:keys [:project/name :project/ref] :as project}]
  [:div.tile
   [:div.tile-content
    [:a.tile-title.h5 {:href (str "#/customers/" (:customer/id project) "/projects/" (:project/id project)) } name]]
   [:div.tile-action
    [:button.btn.btn-link.btn-action.tooltip.tooltip-left.btn-lg
     {:data-tooltip "Create invoice"}
     [:i.icon.icon-mail]]
    [:button.btn.btn-link.btn-action.tooltip.tooltip-left.btn-lg
     {:data-tooltip "Create offer"}
     [:i.icon.icon-share]]]])

(defn customer-tab-item [selected-tab ref title]
  [:li.tab-item
   [:a (merge {:on-click #(reset! selected-tab ref)
               :href "#/customers"}
              (when (= @selected-tab ref) {:class "active"})) title]])


(defn customer-item [{:keys [:customer/email :customer/id :customer/name :customer/contact :customer/city :customer/country :customer/zip :customer/department :customer/street :customer/projects]}]
  (let [selected-tab (r/atom :profile)]
    (fn []
      [:div.column.col-6.col-xs-12
       [:div.panel
        [:div.panel-header.text-center
         [:figure.avatar.avatar-lg {:data-initial (first name)}]
         [:div.panel-title.h5.mt-10 name]
         [:div.panel-subttitle contact]]
        [:nav.panel-nav
         [:ul.tab.tab-block
          [customer-tab-item selected-tab :profile "Profile"]
          [customer-tab-item selected-tab :projects "Projects"]]]

        (if (= @selected-tab :profile)
          [:div.panel-body
           [customer-profile-tile "Email" email]
           [customer-profile-tile "Contact" contact]
           (when-not (clojure.string/blank? department)
             [customer-profile-tile "Department" department])
           [customer-profile-tile "City" city]
           [customer-profile-tile "Zip" zip]
           [customer-profile-tile "Street" street]]
          [:div.panel-body
           (for [project projects]
             [customer-project-tile (assoc project :customer/id id)])
           ])]])))

(defn customer-list-component [state]
  [:div.container
   [:h2 "Customers"]
   (for [[c0 c1] (partition-all 2 (:user/customers @state))]
     [:div.columns
      [customer-item c0]
      [customer-item c1]])])

(defmulti current-page #(-> % deref :page))

(defmethod current-page :home [state]
  [:div.container.grid-lg
   [nav-component state]
   [:div]])

(defmethod current-page :customers [state]
  [:div.container.grid-lg
   [nav-component state]
   [customer-list-component state]
   ])

(defmethod current-page :customer [state]
  [:div.container.grid-lg
   [nav-component state]
   [customer-component state]])

(defmethod current-page :project [state]
  [:div.container.grid-lg
   [nav-component state]
   [project-component state]])

(defmethod current-page :default [state]
  [:div ])

#_(do
  (app-routes)
  (r/render [current-page app-state]
    (. js/document (getElementById "app"))))
(app-routes app-state)
(r/render [current-page app-state]
  (.getElementById js/document "app"))

(defn ^:export main []
  (app-routes app-state)
  (r/render [current-page app-state]
    (.getElementById js/document "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )

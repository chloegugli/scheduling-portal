/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive_grid_cloud_portal.scheduler.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.ow2.proactive_grid_cloud_portal.common.client.model.LogModel;
import org.ow2.proactive_grid_cloud_portal.scheduler.client.model.JobsModel;
import org.ow2.proactive_grid_cloud_portal.scheduler.shared.JobVisuMap;

import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.hydro4ge.raphaelgwt.client.Raphael;
import com.hydro4ge.raphaelgwt.client.Raphael.Rect;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Cursor;
import com.smartgwt.client.types.ImageStyle;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.Img;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.WidgetCanvas;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.MouseStillDownEvent;
import com.smartgwt.client.widgets.events.MouseStillDownHandler;
import com.smartgwt.client.widgets.events.ResizedEvent;
import com.smartgwt.client.widgets.events.ResizedHandler;
import com.smartgwt.client.widgets.events.ScrolledEvent;
import com.smartgwt.client.widgets.events.ScrolledHandler;
import com.smartgwt.client.widgets.layout.Layout;
import com.smartgwt.client.widgets.menu.Menu;


/**
 * Displays an image of the currently selected job if available 
 * 
 * @author mschnoor
 */
public class VisualizationViewImage implements VisualizationView {

    private SchedulerController controller = null;

    private Layout root = null;

    /** displayed when no visu is available */
    private Label message = null;

    /** used to preload the image above */
    private Image imgLoader;

    /** contains the raphael overlay */
    private WidgetCanvas overPane;

    /** overlay to add graphical stuff on the image */
    private Raphael overlay;

    /** coordinates of the tasks on the image */
    private JobVisuMap map;

    /** boxes currently displayed on the raphael canvas */
    private HashMap<String, Box> boxes;

    /** icon displayed on top of the visu view to pop up the navigation view */
    private Canvas navIcon;

    /**
     * Box displayed on the Raphael Canvas :
     * one rectangle, 3 images, 3 labels
     * Label text is editable
     *
     */
    private class Box {
        private String name;

        private Rect failedRect;

        private Rect runningRect;

        private Rect finishedRect;

        private Raphael.Image failedImg;

        private Raphael.Image runningImg;

        private Raphael.Image finishedImg;

        private Raphael.Text running;

        private Raphael.Text finished;

        private Raphael.Text failed;

        private static final int xoff = 11;

        private static final int yoff = -13;

        private static final int icow = 14;

        private static final int icow2 = 24;

        private static final int icod = (icow2 - icow);

        private static final int bheight = 20;

        private static final int toff = 8;

        private static final int yRunningOff = 26;

        private static final int yFailedOff = 2;

        private static final int yFinishedOff = 52;

        private int x, y, w, h;

        Box(String name) {
            this.name = name;
            this.x = map.getTaskX(name) + xoff;
            this.y = map.getTaskY(name) + yoff;
            this.w = map.getTaskW(name);
            this.h = map.getTaskH(name);

            w -= 2;
            x += 1;
            LogModel.getInstance()
                    .logImportantMessage("in constr box " + this.x + "," + this.y + "," + this.w + "," + this.h);
            draw();
        }

        void draw() {
            LogModel.getInstance().logImportantMessage("in draw");
            this.failedRect = overlay.new Rect(x, y, w, h, 5);
            this.failedRect.attr("stroke", "#f49987");
            this.failedRect.hide();

            this.runningRect = overlay.new Rect(x, y, w, h, 5);
            this.runningRect.attr("stroke", "green");
            this.runningRect.hide();

            this.finishedRect = overlay.new Rect(x, y, w, h, 5);
            this.finishedRect.attr("stroke", "#808080");
            this.finishedRect.hide();

            String runningImgUrl = SchedulerImages.instance.running_24().getSafeUri().asString();
            String finishedImgUrl = SchedulerImages.instance.finished_14().getSafeUri().asString();
            String failedImgUrl = SchedulerImages.instance.cancel_14().getSafeUri().asString();

            this.runningImg = overlay.new Image(runningImgUrl, x + w - icow2 - 4, y + yRunningOff, icow2, icow2);
            this.runningImg.hide();
            this.finishedImg = overlay.new Image(finishedImgUrl, x + w - icow - 4 - icod, y + yFinishedOff, icow, icow);
            this.finishedImg.hide();
            this.failedImg = overlay.new Image(failedImgUrl, x + w - icow - 4 - icod, y + yFailedOff, icow, icow);
            this.failedImg.hide();
        }

        void setRunning(int r) {
            if (this.running != null) {
                try {
                    this.running.remove();
                } catch (Exception e) {
                    this.running.hide();
                }
            }
            this.running = overlay.new Text(x + w / 2, y + h / 2, this.name);
            this.running.attr("font-weight", "bold");
            this.running.attr("font-size", "14");
            this.running.attr("text-anchor", "end");

            if (r == 0) {
                this.runningImg.hide();
                this.running.hide();
                this.runningRect.hide();
            } else {
                this.runningImg.show();
                this.running.show();
                this.runningRect.show();
            }
        }

        void setFinished(int f) {
            LogModel.getInstance().logImportantMessage("in setFinished " + f);
            if (this.finished != null) {
                try {
                    this.finished.remove();
                } catch (Exception e) {
                    this.finished.hide();
                }
            }
            this.finished = overlay.new Text(x + w / 2, y + h / 2, this.name);
            this.finished.attr("font-weight", "bold");
            this.finished.attr("font-size", "14");
            this.finished.attr("text-anchor", "end");

            if (f == 0) {
                this.finishedImg.hide();
                this.finished.hide();
                this.finishedRect.hide();
            } else {
                this.finishedImg.show();
                this.finished.show();
                this.finishedRect.show();
            }
        }

        void setFailed(int f) {
            if (this.failed != null) {
                try {
                    this.failed.remove();
                } catch (Exception e) {
                    this.failed.hide();
                }
            }
            this.failed = overlay.new Text(x + w / 2, y + h / 2, this.name);
            this.failed.attr("font-weight", "bold");
            this.failed.attr("font-size", "14");
            this.failed.attr("text-anchor", "end");

            if (f == 0) {
                this.failedImg.hide();
                this.failed.hide();
                this.failedRect.hide();
            } else {
                this.failedImg.show();
                this.failed.show();
                this.failedRect.show();
            }
        }

    }

    VisualizationViewImage(SchedulerController controller) {
        this.controller = controller;
        this.boxes = new HashMap<String, Box>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ow2.proactive_grid_cloud_portal.client.Listeners.VisualizationListener#imageUpdated(java.
     * lang.String, java.lang.String)
     */
    public void imageUpdated(String jobId) {
        JobsModel jobsModel = ((SchedulerModelImpl) controller.getModel()).getExecutionsModel().getJobsModel();
        if (!jobsModel.getSelectedJob().getId().toString().equals(jobId))
            return;

        LogModel.getInstance().logImportantMessage("in imageUpdated");

        int w = 800;//TODO: 800 is hard coded
        int h = 800;

        final int fw = w;
        final int fh = h;

        if (this.overlay != null) {
            this.overlay.clear();
            this.root.removeChild(this.overPane);
            this.overPane = null;
            this.overlay = null;
            this.root.removeChild(this.navIcon);
        }

        this.overlay = new Raphael(w, h);
        this.overPane = new WidgetCanvas(this.overlay);
        this.overPane.setLeft(0);
        this.overPane.setTop(0);
        this.overPane.setWidth(w);
        this.overPane.setHeight(h);

        if (this.map != null) {
            LogModel.getInstance().logImportantMessage("this.map != null");
            for (String name : map.getNames()) {
                this.boxes.put(name, new Box(name));
            }
            this.map = null;
        } else {
            for (Box b : this.boxes.values()) {
                b.draw();
            }
        }

        this.message.setVisible(false);

        this.root.addChild(this.overPane);
        this.root.setWidth(w);
        this.root.setHeight(h);

        // very ugly, only way to control the scroll viewport
        final Canvas scroll = SchedulerPage.inst.visuTab.getPane().getParentElement();

        this.navIcon = new Canvas();
        navIcon.setPosition("absolute");
        navIcon.setBackgroundImage(SchedulerImages.instance.nav_22().getSafeUri().asString());
        navIcon.setZIndex(Integer.MAX_VALUE - 1);
        navIcon.setWidth(22);
        navIcon.setHeight(22);
        navIcon.setCursor(Cursor.HAND);
        navIcon.setTooltip("Navigate the image display");
        updateNavButtonPos(true);

        // HACKED : keep the icon at a fixed position
        scroll.addScrolledHandler(new ScrolledHandler() {
            public void onScrolled(ScrolledEvent event) {
                updateNavButtonPos(false);
            }
        });
        scroll.addResizedHandler(new ResizedHandler() {
            public void onResized(ResizedEvent event) {
                updateNavButtonPos(false);
            }
        });

        root.addChild(navIcon);

        /*
         * pops up a menu that contains a navigation view of the view
         */
        navIcon.addClickHandler(new ClickHandler() { //TODO: Replace with lamba

            public void onClick(ClickEvent event) {
                final int scrollw = scroll.getInnerContentWidth();
                final int scrollh = scroll.getInnerContentHeight();
                final int w = 300;
                final int h = 200;

                final double wmul = (double) fw / (double) w;
                final double hmul = (double) fh / (double) h;

                final Menu menu = new Menu();
                menu.setWidth(w + 2);
                menu.setHeight(h + 2);
                menu.setBorder("1px solid black");
                menu.setShowShadow(true);

                menu.setLeft(navIcon.getAbsoluteLeft() - 4);
                menu.setTop(navIcon.getAbsoluteTop() - h + 16);
                menu.show();
                //root.setContextMenu(menu);
            }
        });
        //Done

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ow2.proactive_grid_cloud_portal.client.Listeners.VisualizationListener#mapUpdated(java.
     * lang.String, org.ow2.proactive_grid_cloud_portal.shared.JobVisuMap)
     */
    public void mapUpdated(String jobId, JobVisuMap map) {
        LogModel.getInstance().logImportantMessage("in mapUpdated image");
        JobsModel jobsModel = ((SchedulerModelImpl) controller.getModel()).getExecutionsModel().getJobsModel();
        if (!jobsModel.getSelectedJob().getId().toString().equals(jobId))
            return;

        this.map = map;
        LogModel.getInstance().logImportantMessage("in mapUpdated image2");
        if (this.overlay != null) {
            for (String name : map.getNames()) {
                LogModel.getInstance().logImportantMessage("adding bb Box");//We don't go there because overlay is null
                Box b = new Box(name);
                this.boxes.put(name, b);
            }
            this.map = null;
            List<Task> tasks = ((SchedulerModelImpl) controller.getModel()).getTasksModel().getTasks();
            this.tasksUpdated(tasks, tasks.size());
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.google.gwt.event.dom.client.LoadHandler#onLoad(com.google.gwt.event.dom.client.LoadEvent)
     */
    public void onLoad(LoadEvent event) {
        LogModel.getInstance().logImportantMessage("in onload");

        // if the image is not visible, it will return a size of 0 in IE9
        imgLoader.setVisible(true);

        int w = imgLoader.getWidth();
        int h = imgLoader.getHeight();

        imgLoader.removeFromParent();

        if (w == 0)
            w = 800;
        if (h == 0)
            h = 800;

        final int fw = w;
        final int fh = h;
        if (this.overlay != null) {
            this.overlay.clear();
            this.root.removeChild(this.overPane);
            this.overPane = null;
            this.overlay = null;
            this.root.removeChild(this.navIcon);
        }

        this.overlay = new Raphael(w, h);
        LogModel.getInstance().logImportantMessage("new Raphael done");
        this.overPane = new WidgetCanvas(this.overlay);
        this.overPane.setLeft(0);
        this.overPane.setTop(0);
        this.overPane.setWidth(w);
        this.overPane.setHeight(h);

        if (this.map != null) {
            for (String name : map.getNames()) {
                this.boxes.put(name, new Box(name));
            }
            this.map = null;
        } else {
            for (Box b : this.boxes.values()) {
                b.draw();
            }
        }

        this.message.setVisible(false);

        this.root.addChild(this.overPane);
        this.root.setWidth(w);
        this.root.setHeight(h);

        // very ugly, only way to control the scroll viewport
        final Canvas scroll = SchedulerPage.inst.visuTab.getPane().getParentElement();

        this.navIcon = new Canvas();
        navIcon.setPosition("absolute");
        navIcon.setBackgroundImage(SchedulerImages.instance.nav_22().getSafeUri().asString());
        navIcon.setZIndex(Integer.MAX_VALUE - 1);
        navIcon.setWidth(22);
        navIcon.setHeight(22);
        navIcon.setCursor(Cursor.HAND);
        navIcon.setTooltip("Navigate the image display");
        updateNavButtonPos(true);

        // HACKED : keep the icon at a fixed position
        scroll.addScrolledHandler(new ScrolledHandler() {
            public void onScrolled(ScrolledEvent event) {
                updateNavButtonPos(false);
            }
        });
        scroll.addResizedHandler(new ResizedHandler() {
            public void onResized(ResizedEvent event) {
                updateNavButtonPos(false);
            }
        });

        root.addChild(navIcon);

        /*
         * pops up a menu that contains a navigation view of the view
         */
        navIcon.addClickHandler(new ClickHandler() {

            public void onClick(ClickEvent event) {
                final int scrollw = scroll.getInnerContentWidth();
                final int scrollh = scroll.getInnerContentHeight();
                final int w = 300;
                final int h = 200;

                final double wmul = (double) fw / (double) w;
                final double hmul = (double) fh / (double) h;

                final Menu menu = new Menu();
                menu.setWidth(w + 2);
                menu.setHeight(h + 2);
                menu.setBorder("1px solid black");
                menu.setShowShadow(true);

                final Canvas rect = new Canvas();
                rect.setWidth((int) ((double) (w * ((double) scrollw / (double) fw))));
                rect.setHeight((int) ((double) (h * ((double) scrollh / (double) fh))));
                rect.setLeft((int) (scroll.getScrollLeft() * ((double) w / (double) fw)));
                rect.setTop((int) (scroll.getScrollTop() * ((double) h / (double) fh)));

                // opacity cannot be used even with IE9 standards mode..
                if (SC.isIE()) {
                    rect.setBorder("2px solid #669ed3");
                } else {
                    rect.setBackgroundColor("#669ed3");
                    rect.setBorder("1px solid black");
                    rect.setOpacity(20);
                }

                rect.setCanDragReposition(true);

                menu.setLeft(navIcon.getAbsoluteLeft() - 4);
                menu.setTop(navIcon.getAbsoluteTop() - h + 16);
                menu.show();
                //root.setContextMenu(menu);
            }
        });
        LogModel.getInstance().logImportantMessage("setting tasks");

        List<Task> tasks = ((SchedulerModelImpl) controller.getModel()).getTasksModel().getTasks();
        int i = 0;
        for (Task t : tasks) {
            i++;
            map.addTask(10 + i, 10 + i, 20, 20, t.getName());
            LogModel.getInstance().logImportantMessage("in for task");
        }
        this.tasksUpdated(tasks, tasks.size());
    }

    private void updateNavButtonPos(boolean init) {
        // very ugly, only way to control the scroll viewport
        final Canvas scroll = SchedulerPage.inst.visuTab.getPane().getParentElement();

        int scrollTop = scroll.getScrollLeft() + scroll.getInnerContentWidth() - 34;

        if (init && scroll.getInnerContentWidth() != scroll.getWidth())
            scrollTop -= scroll.getScrollbarSize();

        navIcon.setLeft(scrollTop);
        navIcon.setTop(scroll.getScrollTop() + 5);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ow2.proactive_grid_cloud_portal.client.Listeners.JobSelectedListener#jobSelected(org.ow2.
     * proactive_grid_cloud_portal.client.Job)
     */
    public void jobSelected(Job job) {
        if (this.overlay != null) {
            this.overlay.clear();
            this.root.removeChild(this.overPane);
            this.boxes.clear();
            this.overPane = null;
            this.overlay = null;
            this.root.removeChild(this.navIcon);
        }
        LogModel.getInstance().logImportantMessage("setting loading");
        this.message.setContents("Loading...");
        this.message.setIcon("loading.gif"); // it is safe to ignore the "image 'undefined' couldn't be found" error
        this.message.setVisible(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ow2.proactive_grid_cloud_portal.client.Listeners.JobSelectedListener#jobUnselected()
     */
    public void jobUnselected() {
        if (this.overlay != null) {
            this.overlay.clear();
            this.root.removeChild(this.overPane);
            this.boxes.clear();
            this.overPane = null;
            this.overlay = null;
            this.root.removeChild(this.navIcon);
        }
        this.message.setContents("No job selected.");
        this.message.setIcon(null);
        this.message.setVisible(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ow2.proactive_grid_cloud_portal.client.Listeners.VisualizationListener#
     * visualizationUnavailable(java.lang.String)
     */
    public void visualizationUnavailable(String jobId) {
        this.message.setContents("Visualization is not available<br><br>" +
                                 "To use visualization you need to create your job using the <strong>Workflow Studio</strong>, and <br>" +
                                 "submit it directly from the editor");
        this.message.setIcon(null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.ow2.proactive_grid_cloud_portal.client.Listeners.TasksUpdatedListener#tasksUpdating(
     * boolean)
     */
    public void tasksUpdating() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ow2.proactive_grid_cloud_portal.client.Listeners.TasksUpdatedListener#tasksUpdated(java.
     * util.List)
     */
    public void tasksUpdated(List<Task> tasks, long totalTasks) {
        LogModel.getInstance().logImportantMessage("in tasksUpdated");

        if (this.boxes.isEmpty() || ((SchedulerModelImpl) controller.getModel()).getTasksModel().isTasksDirty()) {
            return;
        }

        HashMap<String, VisuTaskStatus> ft = new HashMap<String, VisuTaskStatus>();

        for (Task t : tasks) {
            String name = t.getName();
            int i1 = name.indexOf('#');

            if (i1 > -1)
                name = name.substring(0, i1);

            int i2 = name.indexOf('*');
            if (i2 > -1)
                name = name.substring(0, i2);

            if (ft.get(name) == null)
                ft.put(name, new VisuTaskStatus());

            switch (t.getStatus()) {
                case ABORTED:
                    ft.get(name).failed++;
                    break;
                case FAILED:
                    ft.get(name).failed++;
                    break;
                case FAULTY:
                    ft.get(name).failed++;
                    break;
                case FINISHED:
                    ft.get(name).finished++;
                    break;
                case NOT_RESTARTED:
                    ft.get(name).failed++;
                    break;
                case NOT_STARTED:
                    ft.get(name).failed++;
                    break;
                case PAUSED:
                case IN_ERROR:
                    break;
                case PENDING:
                    break;
                case RUNNING:
                    ft.get(name).running++;
                    break;
                /*
                 * skipped tasks are somewhat finished but
                 * we don't want them appearing as such on the screen
                 * case SKIPPED:
                 * ft.get(name).finished++;
                 * break;
                 */
                case SUBMITTED:
                    break;
                case WAITING_ON_ERROR:
                    ft.get(name).running++;
                    break;
                case WAITING_ON_FAILURE:
                    ft.get(name).running++;
                    break;
            }
        }

        for (Entry<String, Box> b : this.boxes.entrySet()) {
            VisuTaskStatus ts = ft.get(b.getKey());
            if (ts == null)
                continue;

            Box box = b.getValue();
            box.setRunning(ts.running);
            box.setFailed(ts.failed);
            box.setFinished(ts.finished);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.ow2.proactive_grid_cloud_portal.client.Listeners.TasksUpdatedListener#tasksUpdatedFailure
     * (java.lang.String)
     */
    public void tasksUpdatedFailure(String message) {
    }

    @Override
    public void htmlUpdated(String jobId, String path) {
        jobUnselected();
        this.message.setVisible(false);
        //TODO: de we need that??
    }

    public void setRoot(Layout layout) {
        this.root = layout;

        this.message = new Label("No job selected");
        this.message.setAlign(Alignment.CENTER);
        this.message.setWidth100();

        this.root.addMember(message);
    }

    @Override
    public void selectedJobUpdated(Job job) {
        jobSelected(job);
    }
}

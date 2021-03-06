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
package org.ow2.proactive_grid_cloud_portal.common.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.LinkElement;


/**
 * Native JS Utils for stuff that is not already wrapped by [Smart]GWT.
 * 
 * 
 * @author mschnoor
 *
 */
public class JSUtil {

    public interface JSCallback {
        public void execute(JavaScriptObject obj);
    }

    private static int requestCounter = 0;

    /**
    * Register a native JS callback to the document
    * for asynchronous callback in SmartGWT forms that do not support it
    * 
    * Simply register the function, keep the name and recall it later
    * 	
    * @param callback
    * @return name of the callback
    */
    public static String register(JSCallback callback) {
        String callbackName = "callback" + (requestCounter++);
        createCallbackFunction(callback, callbackName);
        return callbackName;
    }

    private native static void createCallbackFunction(JSCallback obj, String callbackName)/*-{
                                                                                          tmpcallback = function(j) {
                                                                                          obj.@org.ow2.proactive_grid_cloud_portal.common.client.JSUtil.JSCallback::execute(Lcom/google/gwt/core/client/JavaScriptObject;)( j );
                                                                                          };
                                                                                          $wnd[callbackName] = tmpcallback;
                                                                                          }-*/;

    /**
     * Load scripts in the document, in order, one after another.
     * 
     * @param paths relative paths to the JS
     */
    public static void addScript(String... paths) {
        List<String> pathAsList = new ArrayList<String>(Arrays.asList(paths));
        injectScriptOneAfterAnother(pathAsList);
    }

    private static void injectScriptOneAfterAnother(final List<String> pathAsList) {
        ScriptInjector.fromUrl(pathAsList.remove(0))
                      .setWindow(ScriptInjector.TOP_WINDOW)
                      .setCallback(new Callback<Void, Exception>() {
                          @Override
                          public void onFailure(Exception reason) {
                          }

                          @Override
                          public void onSuccess(Void result) {
                              if (!pathAsList.isEmpty()) {
                                  injectScriptOneAfterAnother(pathAsList);
                              }
                          }
                      })
                      .inject();
    }

    public static void addStyle(String path) {
        Element head = Document.get().getElementsByTagName("head").getItem(0);
        LinkElement style = Document.get().createLinkElement();
        style.setPropertyString("language", "text/css");
        style.setRel("stylesheet");
        style.setHref(path);
        head.appendChild(style);
    }

    /**
     * @return available screen width in pixels
     */
    public static int getScreenWidth() {
        return Integer.parseInt(getAvailWidth());
    }

    /**
     * @return available screen height in pixels
     */
    public static int getScreenHeight() {
        return Integer.parseInt(getAvailHeight());
    }

    private static native String getAvailHeight() /*-{
                                                  return screen.availHeight + "";
                                                  }-*/;

    private static native String getAvailWidth() /*-{
                                                 return screen.availWidth + "";
                                                 }-*/;

    public static String getTime(long time) {
        if (time < 0) {
            return "";
        }

        StringBuilder ret = new StringBuilder();
        ret.append(new Date(time).toString());
        ret.append(", ");

        long seconds = (System.currentTimeMillis() - time) / 1000;
        long day, hou, min, sec;

        day = seconds / (3600 * 24);
        seconds -= day * (3600 * 24);
        hou = seconds / 3600;
        seconds -= hou * 3600;
        min = seconds / 60;
        sec = seconds % 60;

        if (seconds < 0) {
            day = Math.abs(day);
            hou = Math.abs(hou);
            min = Math.abs(min);
            sec = Math.abs(sec);
            ret.append("in ");
        }

        if (day > 0) {
            ret.append(day).append("d ");
            ret.append(hou).append("h ");
        } else if (hou > 0) {
            ret.append(hou).append("h");
            ret.append(min).append("mn ");
        } else if (min > 0) {
            ret.append(min).append("mn ");
        } else if (sec > 0) {
            ret.append(sec).append("s ");
        }

        if (seconds > 0) {
            ret.append("ago");
        }

        return ret.toString();
    }
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.opensymphony.xwork2.util;

import com.mockobjects.dynamic.Mock;
import com.opensymphony.xwork2.*;
import com.opensymphony.xwork2.config.providers.XmlConfigurationProvider;
import com.opensymphony.xwork2.test.ModelDrivenAction2;
import com.opensymphony.xwork2.test.TestBean2;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * Unit test for {@link StrutsLocalizedTextProvider}.
 *
 * @author jcarreira
 * @author tm_jee
 * 
 * @version $Date$ $Id$
 */
public class StrutsLocalizedTextProviderTest extends XWorkTestCase {

    private LocalizedTextProvider localizedTextProvider;
    
	public void testNpeWhenClassIsPrimitive() throws Exception {
		ValueStack stack = ActionContext.getContext().getValueStack();
		stack.push(new MyObject());
		String result = localizedTextProvider.findText(MyObject.class, "someObj.someI18nKey", Locale.ENGLISH, "default message", null, stack);
		System.out.println(result);
	}
	
	public static class MyObject extends ActionSupport {
		public boolean getSomeObj() {
			return true;
		}
	}
	
	public void testActionGetTextWithNullObject() throws Exception {
		MyAction action = new MyAction();
        container.inject(action);
		
		Mock mockActionInvocation = new Mock(ActionInvocation.class);
        mockActionInvocation.expectAndReturn("getAction", action);
        ActionContext.getContext()
            .withActionInvocation((ActionInvocation) mockActionInvocation.proxy())
		    .getValueStack().push(action);
		
		String message = action.getText("barObj.title");
		assertEquals("Title:", message);
	}
	
	
	public static class MyAction extends ActionSupport {
		private Bar testBean2;
		
		public Bar getBarObj() {
			return testBean2;
		}
		public void setBarObj(Bar testBean2) {
			this.testBean2 = testBean2;
		}
	}
	
    public void testActionGetText() throws Exception {
        ModelDrivenAction2 action = new ModelDrivenAction2();
        container.inject(action);

        TestBean2 bean = (TestBean2) action.getModel();
        Bar bar = new Bar();
        bean.setBarObj(bar);

        Mock mockActionInvocation = new Mock(ActionInvocation.class);
        mockActionInvocation.expectAndReturn("getAction", action);
        ActionContext.getContext().withActionInvocation((ActionInvocation) mockActionInvocation.proxy());
        ActionContext.getContext().getValueStack().push(action);
        ActionContext.getContext().getValueStack().push(action.getModel());

        String message = action.getText("barObj.title");
        assertEquals("Title:", message);
    }

    public void testNullKeys() {
        localizedTextProvider.findText(this.getClass(), null, Locale.getDefault());
    }

    public void testActionGetTextXXX() throws Exception {
        localizedTextProvider.addDefaultResourceBundle("com/opensymphony/xwork2/util/FindMe");

        SimpleAction action = new SimpleAction();
        container.inject(action);

        Mock mockActionInvocation = new Mock(ActionInvocation.class);
        mockActionInvocation.expectAndReturn("getAction", action);
        ActionContext.getContext()
            .withActionInvocation((ActionInvocation) mockActionInvocation.proxy())
            .getValueStack().push(action);

        String message = action.getText("bean.name");
        String foundBean2 = action.getText("bean2.name");

        assertEquals("Okay! You found Me!", foundBean2);
        assertEquals("Haha you cant FindMe!", message);
    }

    public void testAddDefaultResourceBundle() {
        String text = localizedTextProvider.findDefaultText("foo.range", Locale.getDefault());
        assertNull("Found message when it should not be available.", text);

        localizedTextProvider.addDefaultResourceBundle("com/opensymphony/xwork2/SimpleAction");

        String message = localizedTextProvider.findDefaultText("foo.range", Locale.US);
        assertEquals("Foo Range Message", message);
    }

    public void testAddDefaultResourceBundle2() throws Exception {
        localizedTextProvider.addDefaultResourceBundle("com/opensymphony/xwork2/SimpleAction");

        ActionProxy proxy = actionProxyFactory.createActionProxy("/", "packagelessAction", null, new HashMap<String, Object>(), false, true);
        proxy.execute();
    }

    public void testDefaultMessage() throws Exception {
        String message = localizedTextProvider.findDefaultText("xwork.error.action.execution", Locale.getDefault());
        assertEquals("Error during Action invocation", message);
    }

    public void testDefaultMessageOverride() throws Exception {
        String message = localizedTextProvider.findDefaultText("xwork.error.action.execution", Locale.getDefault());
        assertEquals("Error during Action invocation", message);

        localizedTextProvider.addDefaultResourceBundle("com/opensymphony/xwork2/test");

        message = localizedTextProvider.findDefaultText("xwork.error.action.execution", Locale.getDefault());
        assertEquals("Testing resource bundle override", message);
    }

    public void testFindTextInChildProperty() throws Exception {
        ModelDriven action = new ModelDrivenAction2();
        TestBean2 bean = (TestBean2) action.getModel();
        Bar bar = new Bar();
        bean.setBarObj(bar);

        Mock mockActionInvocation = new Mock(ActionInvocation.class);
        mockActionInvocation.expectAndReturn("hashCode", 0);
        mockActionInvocation.expectAndReturn("getAction", action);
        ActionContext.getContext().withActionInvocation((ActionInvocation) mockActionInvocation.proxy());
        ActionContext.getContext().getValueStack().push(action);
        ActionContext.getContext().getValueStack().push(action.getModel());

        String message = localizedTextProvider.findText(ModelDrivenAction2.class, "invalid.fieldvalue.barObj.title", Locale.getDefault());
        assertEquals("Title is invalid!", message);
    }

    public void testFindTextInInterface() throws Exception {
        Action action = new ModelDrivenAction2();
        Mock mockActionInvocation = new Mock(ActionInvocation.class);
        mockActionInvocation.expectAndReturn("getAction", action);
        ActionContext.getContext().withActionInvocation((ActionInvocation) mockActionInvocation.proxy());

        String message = localizedTextProvider.findText(ModelDrivenAction2.class, "test.foo", Locale.getDefault());
        assertEquals("Foo!", message);
    }

    public void testFindTextInPackage() throws Exception {
        ModelDriven action = new ModelDrivenAction2();

        Mock mockActionInvocation = new Mock(ActionInvocation.class);
        mockActionInvocation.expectAndReturn("getAction", action);
        ActionContext.getContext().withActionInvocation((ActionInvocation) mockActionInvocation.proxy());

        String message = localizedTextProvider.findText(ModelDrivenAction2.class, "package.properties", Locale.getDefault());
        assertEquals("It works!", message);
    }

    public void testParameterizedDefaultMessage() throws Exception {
        String message = localizedTextProvider.findDefaultText("xwork.exception.missing-action", Locale.getDefault(), new String[]{"AddUser"});
        assertEquals("There is no Action mapped for action name AddUser.", message);
    }

    public void testParameterizedDefaultMessageWithPackage() throws Exception {
        String message = localizedTextProvider.findDefaultText("xwork.exception.missing-package-action", Locale.getDefault(), new String[]{"blah", "AddUser"});
        assertEquals("There is no Action mapped for namespace blah and action name AddUser.", message);
    }

    public void testLocalizedDateFormatIsUsed() throws ParseException {
        localizedTextProvider.addDefaultResourceBundle("com/opensymphony/xwork2/util/LocalizedTextUtilTest");
        Date date = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US).parse("01/01/2015");
        Object[] params = new Object[]{ date };
        String usDate = localizedTextProvider.findDefaultText("test.format.date", Locale.US, params);
        String germanDate = localizedTextProvider.findDefaultText("test.format.date", Locale.GERMANY, params);
        assertEquals(usDate, "1/1/15");
        assertEquals(germanDate, "01.01.15");
    }

    public void testXW377() {
        localizedTextProvider.addDefaultResourceBundle("com/opensymphony/xwork2/util/LocalizedTextUtilTest");

        String text = localizedTextProvider.findText(Bar.class, "xw377", ActionContext.getContext().getLocale(), "xw377", null, ActionContext.getContext().getValueStack());
        assertEquals("xw377", text); // should not log

        String text2 = localizedTextProvider.findText(StrutsLocalizedTextProviderTest.class, "notinbundle", ActionContext.getContext().getLocale(), "hello", null, ActionContext.getContext().getValueStack());
        assertEquals("hello", text2); // should log WARN

        String text3 = localizedTextProvider.findText(StrutsLocalizedTextProviderTest.class, "notinbundle.key", ActionContext.getContext().getLocale(), "notinbundle.key", null, ActionContext.getContext().getValueStack());
        assertEquals("notinbundle.key", text3); // should log WARN

        String text4 = localizedTextProvider.findText(StrutsLocalizedTextProviderTest.class, "xw377", ActionContext.getContext().getLocale(), "hello", null, ActionContext.getContext().getValueStack());
        assertEquals("xw377", text4); // should not log

        String text5 = localizedTextProvider.findText(StrutsLocalizedTextProviderTest.class, "username", ActionContext.getContext().getLocale(), null, null, ActionContext.getContext().getValueStack());
        assertEquals("Santa", text5); // should not log
    }

    public void testXW404() {
        // This tests will try to load bundles from the 3 locales but we only have files for France and Germany.
        // Before this fix loading the bundle for Germany failed since Italy have previously failed and thus the misses cache
        // contained a false entry

        ResourceBundle rbFrance = localizedTextProvider.findResourceBundle("com/opensymphony/xwork2/util/XW404", Locale.FRANCE);
        ResourceBundle rbItaly = localizedTextProvider.findResourceBundle("com/opensymphony/xwork2/util/XW404", Locale.ITALY);
        ResourceBundle rbGermany = localizedTextProvider.findResourceBundle("com/opensymphony/xwork2/util/XW404", Locale.GERMANY);

        assertNotNull(rbFrance);
        assertEquals("Bonjour", rbFrance.getString("hello"));

        assertNull(rbItaly);

        assertNotNull(rbGermany);
        assertEquals("Hallo", rbGermany.getString("hello"));
    }

    /**
     * Unit test to confirm expected behaviour of "clearing methods" provided to
     *   StrutsLocalizedTextProvider (from AbstractLocalizedTextProvider).
     *
     * @since 2.6
     */
    public void testLocalizedTextProviderClearingMethods() {
        TestStrutsLocalizedTextProvider testStrutsLocalizedTextProvider = new TestStrutsLocalizedTextProvider();
        assertTrue("testStrutsLocalizedTextProvider not instance of AbstractLocalizedTextProvider ?",
                testStrutsLocalizedTextProvider instanceof AbstractLocalizedTextProvider);
        assertEquals("testStrutsLocalizedTextProvider starting default bundle map size not 0 before any retrievals ?",
                0, testStrutsLocalizedTextProvider.currentBundlesMapSize());

        // Access the two default bundles to populate their cache entries and test bundle map size.
        ResourceBundle tempBundle = testStrutsLocalizedTextProvider.findResourceBundle(
                TestStrutsLocalizedTextProvider.XWORK_MESSAGES_BUNDLE, Locale.ENGLISH);
        assertNotNull("XWORK_MESSAGES_BUNDLE retrieval null ?", tempBundle);
        tempBundle = testStrutsLocalizedTextProvider.findResourceBundle(
                TestStrutsLocalizedTextProvider.STRUTS_MESSAGES_BUNDLE, Locale.ENGLISH);
        assertNotNull("STRUTS_MESSAGES_BUNDLE retrieval null ?", tempBundle);
        assertEquals("testStrutsLocalizedTextProvider bundle map size not 2 after retrievals ?",
                2, testStrutsLocalizedTextProvider.currentBundlesMapSize());

        // Add and then access four test bundles to populate their cache entries and test bundle map size.
        testStrutsLocalizedTextProvider.addDefaultResourceBundle("com/opensymphony/xwork2/util/LocalizedTextUtilTest");
        testStrutsLocalizedTextProvider.addDefaultResourceBundle("com/opensymphony/xwork2/util/FindMe");
        testStrutsLocalizedTextProvider.addDefaultResourceBundle("com/opensymphony/xwork2/SimpleAction");
        testStrutsLocalizedTextProvider.addDefaultResourceBundle("com/opensymphony/xwork2/test");
        tempBundle = testStrutsLocalizedTextProvider.findResourceBundle(
                "com/opensymphony/xwork2/util/LocalizedTextUtilTest", Locale.ENGLISH);
        assertNotNull("com/opensymphony/xwork2/util/LocalizedTextUtilTest retrieval null ?", tempBundle);
        tempBundle = testStrutsLocalizedTextProvider.findResourceBundle(
                "com/opensymphony/xwork2/util/FindMe", Locale.ENGLISH);
        assertNotNull("com/opensymphony/xwork2/util/FindMe retrieval null ?", tempBundle);
        tempBundle = testStrutsLocalizedTextProvider.findResourceBundle(
                "com/opensymphony/xwork2/SimpleAction", Locale.ENGLISH);
        assertNotNull("com/opensymphony/xwork2/SimpleAction retrieval null ?", tempBundle);
        tempBundle = testStrutsLocalizedTextProvider.findResourceBundle(
                "com/opensymphony/xwork2/test", Locale.ENGLISH);
        assertNotNull("com/opensymphony/xwork2/test retrieval null ?", tempBundle);
        assertEquals("testStrutsLocalizedTextProvider bundle map size not 6 after retrievals ?",
                6, testStrutsLocalizedTextProvider.currentBundlesMapSize());

        // Expect the call to be ineffective due to deprecation and change to a "no-op" (but shouldn't throw an Exception or cause failure).
        testStrutsLocalizedTextProvider.callClearBundleNoLocale("com/opensymphony/xwork2/test");
        assertEquals("testStrutsLocalizedTextProvider bundle map size not 6 after non-locale clear call ?",
                6, testStrutsLocalizedTextProvider.currentBundlesMapSize());

        // Expect the call to function with bundle name + locale.  Remove all four of the non-default
        //   bundles and confirm the bundle map size changes.
        testStrutsLocalizedTextProvider.callClearBundleWithLocale("com/opensymphony/xwork2/test", Locale.ENGLISH);
        assertEquals("testStrutsLocalizedTextProvider bundle map size not 5 after locale clear call ?",
                5, testStrutsLocalizedTextProvider.currentBundlesMapSize());
        testStrutsLocalizedTextProvider.callClearBundleWithLocale("com/opensymphony/xwork2/SimpleAction", Locale.ENGLISH);
        assertEquals("testStrutsLocalizedTextProvider bundle map size not 4 after locale clear call ?",
                4, testStrutsLocalizedTextProvider.currentBundlesMapSize());
        testStrutsLocalizedTextProvider.callClearBundleWithLocale("com/opensymphony/xwork2/util/FindMe", Locale.ENGLISH);
        assertEquals("testStrutsLocalizedTextProvider bundle map size not 3 after locale clear call ?",
                3, testStrutsLocalizedTextProvider.currentBundlesMapSize());
        testStrutsLocalizedTextProvider.callClearBundleWithLocale("com/opensymphony/xwork2/util/LocalizedTextUtilTest", Locale.ENGLISH);
        assertEquals("testStrutsLocalizedTextProvider bundle map size not 2 after locale clear call ?",
                2, testStrutsLocalizedTextProvider.currentBundlesMapSize());

        // Confirm the missing bundles cache clearing method does not produce any Exceptions or failures.
        testStrutsLocalizedTextProvider.callClearMissingBundlesCache();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        XmlConfigurationProvider provider = new XmlConfigurationProvider("xwork-sample.xml");
        container.inject(provider);
        loadConfigurationProviders(provider);

        localizedTextProvider = container.getInstance(LocalizedTextProvider.class);
        
        ActionContext.getContext().withLocale(Locale.US);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        localizedTextProvider = null;
    }

    /**
     * Basic test class to allow specific testing of StrutsLocalizedTextProvider.
     *
     * @since 2.6
     */
    class TestStrutsLocalizedTextProvider extends StrutsLocalizedTextProvider {

        public void callClearBundleNoLocale(String bundleName) {
            super.clearBundle(bundleName);
}

        public void callClearBundleWithLocale(String bundleName, Locale locale) {
            super.clearBundle(bundleName, locale);
        }

        public void callClearMissingBundlesCache() {
            super.clearMissingBundlesCache();
        }

        public int currentBundlesMapSize() {
            return super.bundlesMap.size();
        }
    }
}

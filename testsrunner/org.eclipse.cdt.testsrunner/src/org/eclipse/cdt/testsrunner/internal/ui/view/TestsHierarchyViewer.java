/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.internal.ui.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.internal.ui.viewsupport.ColoringLabelProvider;
import org.eclipse.cdt.testsrunner.internal.TestsRunnerPlugin;
import org.eclipse.cdt.testsrunner.internal.ui.view.actions.CopySelectedTestsAction;
import org.eclipse.cdt.testsrunner.internal.ui.view.actions.RerunSelectedAction;
import org.eclipse.cdt.testsrunner.model.IModelVisitor;
import org.eclipse.cdt.testsrunner.model.ITestCase;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestMessage;
import org.eclipse.cdt.testsrunner.model.ITestSuite;
import org.eclipse.cdt.testsrunner.model.ITestingSession;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.actions.ActionFactory;

/**
 * TODO: Add description here
 * TODO: fix header comment
 */
public class TestsHierarchyViewer {
	
	class TestTreeContentProvider implements ITreeContentProvider {

		class TestCasesCollector implements IModelVisitor {
			
			List<ITestCase> testCases = new ArrayList<ITestCase>();
			
			public void visit(ITestCase testCase) {
				testCases.add(testCase);
			}
			
			public void visit(ITestMessage testMessage) {}
			public void visit(ITestSuite testSuite) {}
			public void leave(ITestSuite testSuite) {}
			public void leave(ITestCase testCase) {}
			public void leave(ITestMessage testMessage) {}
		}
		
		public Object[] getChildren(Object parentElement) {
			return ((ITestItem) parentElement).getChildren();
		}

		public Object[] getElements(Object rootTestSuite) {
			if (showTestsHierarchy) {
				return getChildren(rootTestSuite);
			} else {
				TestCasesCollector testCasesCollector = new TestCasesCollector();
				((ITestItem)rootTestSuite).visit(testCasesCollector);
				return testCasesCollector.testCases.toArray();
			}
		}

		public Object getParent(Object object) {
			return ((ITestItem) object).getParent();
		}

		public boolean hasChildren(Object object) {
			return ((ITestItem) object).hasChildren();
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

	}

	class TestLabelProvider extends LabelProvider implements IStyledLabelProvider {

		private Map<ITestItem.Status, Image> testCaseImages = new HashMap<ITestItem.Status, Image>();
		{
			testCaseImages.put(ITestItem.Status.NotRun, TestsRunnerPlugin.createAutoImage("obj16/test_notrun.gif")); //$NON-NLS-1$
			testCaseImages.put(ITestItem.Status.Skipped, TestsRunnerPlugin.createAutoImage("obj16/test_skipped.gif")); //$NON-NLS-1$
			testCaseImages.put(ITestItem.Status.Passed, TestsRunnerPlugin.createAutoImage("obj16/test_passed.gif")); //$NON-NLS-1$
			testCaseImages.put(ITestItem.Status.Failed, TestsRunnerPlugin.createAutoImage("obj16/test_failed.gif")); //$NON-NLS-1$
			testCaseImages.put(ITestItem.Status.Aborted, TestsRunnerPlugin.createAutoImage("obj16/test_aborted.gif")); //$NON-NLS-1$
		}
		private Image testCaseRunImage = TestsRunnerPlugin.createAutoImage("obj16/test_run.gif"); //$NON-NLS-1$


		private Map<ITestItem.Status, Image> testSuiteImages = new HashMap<ITestItem.Status, Image>();
		{
			// NOTE: There is no skipped-icon for test suite, but it seems it is not a problem
			testSuiteImages.put(ITestItem.Status.NotRun, TestsRunnerPlugin.createAutoImage("obj16/tsuite_notrun.gif")); //$NON-NLS-1$
			testSuiteImages.put(ITestItem.Status.Skipped, TestsRunnerPlugin.createAutoImage("obj16/tsuite_notrun.gif")); //$NON-NLS-1$
			testSuiteImages.put(ITestItem.Status.Passed, TestsRunnerPlugin.createAutoImage("obj16/tsuite_passed.gif")); //$NON-NLS-1$
			testSuiteImages.put(ITestItem.Status.Failed, TestsRunnerPlugin.createAutoImage("obj16/tsuite_failed.gif")); //$NON-NLS-1$
			testSuiteImages.put(ITestItem.Status.Aborted, TestsRunnerPlugin.createAutoImage("obj16/tsuite_aborted.gif")); //$NON-NLS-1$
		}
		private Image testSuiteRunImage = TestsRunnerPlugin.createAutoImage("obj16/tsuite_run.gif"); //$NON-NLS-1$


	    public Image getImage(Object element) {
	    	Map<ITestItem.Status, Image> imagesMap = null;
	    	Image runImage = null;
	    	if (element instanceof ITestCase) {
	    		imagesMap = testCaseImages;
	    		runImage = testCaseRunImage;
	    		
	    	} else if (element instanceof ITestSuite) {
	    		imagesMap = testSuiteImages;
	    		runImage = testSuiteRunImage;
	    	}
	    	if (imagesMap != null) {
	    		ITestItem testItem = (ITestItem)element;
				if (testingSession.getModelAccessor().isCurrentlyRunning(testItem)) {
					return runImage;
				}
				return imagesMap.get(testItem.getStatus());
	    	}
	    	
	    	return null;
	    }

		public String getText(Object element) {
			ITestItem testItem = (ITestItem)element;
			StringBuilder sb = new StringBuilder();
			sb.append(testItem.getName());
			if (!showTestsHierarchy) {
				sb.append(TestPathUtils.getTestItemPath(testItem));
			}
			if (showTime) {
				sb.append(getTestingTimeString(element));
			}
			return sb.toString();
		}

		public StyledString getStyledText(Object element) {
			ITestItem testItem = (ITestItem)element;
			StringBuilder labelBuf = new StringBuilder();
			labelBuf.append(testItem.getName());
			StyledString name = new StyledString(labelBuf.toString());
			if (!showTestsHierarchy) {
				String itemPath = " - "+TestPathUtils.getTestItemPath(testItem);
				labelBuf.append(itemPath);
				name = StyledCellLabelProvider.styleDecoratedString(labelBuf.toString(), StyledString.QUALIFIER_STYLER, name);
			}
			if (showTime) {
				String time = getTestingTimeString(element);
				labelBuf.append(time);
				name = StyledCellLabelProvider.styleDecoratedString(labelBuf.toString(), StyledString.COUNTER_STYLER, name);
			}
			return name;
		}
		
		private String getTestingTimeString(Object element) {
			// TODO: Add a message template and internalize it!
			return (element instanceof ITestItem) ? " ("+Double.toString(((ITestItem)element).getTestingTime()/1000.0)+" s)" : "";
		}
		
	}
	
	class FailedOnlyFilter extends ViewerFilter {

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			return ((ITestItem)element).getStatus().isError();
		}
	}

	
	private ITestingSession testingSession;
	private TreeViewer treeViewer;
	private boolean showTime = true;
	private FailedOnlyFilter failedOnlyFilter = null;
	private boolean showTestsHierarchy = true;
	private Clipboard clipboard;
	private Action copyAction;
	private Action rerunAction;

	
	public TestsHierarchyViewer(Composite parent, IViewSite viewSite, Clipboard clipboard) {
		this.clipboard = clipboard;
		treeViewer = new TreeViewer(parent, SWT.V_SCROLL | SWT.MULTI);
		treeViewer.setContentProvider(new TestTreeContentProvider());
		treeViewer.setLabelProvider(new ColoringLabelProvider(new TestLabelProvider()));
		initContextMenu(viewSite);
	}
	
	private void initContextMenu(IViewSite viewSite) {
		copyAction = new CopySelectedTestsAction(treeViewer, clipboard);
		rerunAction = new RerunSelectedAction(testingSession, treeViewer);

		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				handleMenuAboutToShow(manager);
			}
		});
		viewSite.registerContextMenu(menuMgr, treeViewer);
		Menu menu = menuMgr.createContextMenu(treeViewer.getTree());
		treeViewer.getTree().setMenu(menu);

		menuMgr.add(new Separator());
		menuMgr.add(rerunAction);
		menuMgr.add(copyAction);

		IActionBars actionBars = viewSite.getActionBars();
		actionBars.setGlobalActionHandler(ActionFactory.COPY.getId(), copyAction);
		actionBars.updateActionBars();
	}
	
	private void handleMenuAboutToShow(IMenuManager manager) {
		IStructuredSelection selection = (IStructuredSelection)treeViewer.getSelection();
		rerunAction.setEnabled(
			!selection.isEmpty() && 
			(testingSession.getTestsRunnerInfo().isAllowedMultipleTestFilter() || (selection.size() == 1))
		);
		copyAction.setEnabled(!selection.isEmpty());
	}

	public void setTestingSession(ITestingSession testingSession) {
		this.testingSession = testingSession;
		treeViewer.setInput(testingSession != null ? testingSession.getModelAccessor().getRootSuite() : null);
	}
	
	public TreeViewer getTreeViewer() {
		return treeViewer;
	}

	public void showNextFailure() {
		showFailure(true);
	}
	
	public void showPreviousFailure() {
		showFailure(false);
	}
	
	private void showFailure(boolean next) {
		IStructuredSelection selection = (IStructuredSelection) getTreeViewer().getSelection();
		ITestItem selected = (ITestItem) selection.getFirstElement();
		ITestItem failedItem;

		if (selected == null) {
			ITestItem rootSuite = (ITestItem)treeViewer.getInput();
			// For next element we should also check its children, for previous shouldn't.
			failedItem = findFailedImpl(rootSuite, null, next, next);
		} else {
			// For next element we should also check its children, for previous shouldn't.
			failedItem = findFailedImpl(selected.getParent(), selected, next, next);
		}

		if (failedItem != null)
			getTreeViewer().setSelection(new StructuredSelection(failedItem), true);
	}
	
	private ITestItem findFailedImpl(ITestItem parentItem, ITestItem currItem, boolean next, boolean checkCurrentChild) {
		ITestItem result = findFailedChild(parentItem, currItem, next, checkCurrentChild);
		if (result != null) {
			return result;
		}
		// Nothing found at this level - try to step up
		ITestSuite grandParentItem = parentItem.getParent();
		if (grandParentItem != null) {
			return findFailedImpl(grandParentItem, parentItem, next, false);
		}
		return null;
	}
	
	private ITestItem findFailedChild(ITestItem parentItem, ITestItem currItem, boolean next, boolean checkCurrentChild) {
		ITestItem[] children = parentItem.getChildren();
		boolean doSearch = (currItem == null);
		int increment = next ? 1 : -1;
		int startIndex = next ? 0 : children.length-1;
		int endIndex = next ? children.length : -1;
		for (int index = startIndex; index != endIndex; index += increment) {
			ITestItem item = children[index];
			// Check element
			if (doSearch) {
				if (item instanceof ITestCase && item.getStatus().isError()) {
					return item;
				}
			}
			// If children of current element should be checked we should enable search here (if necessary)
			if (checkCurrentChild && item == currItem) {
				doSearch = true;
			}
			// Search element's children
			if (doSearch) {
				ITestItem result = findFailedChild(item, null, next, checkCurrentChild);
				if (result != null) {
					return result;
				}
			}
			// If children of current element should NOT be checked we should enable search here
			if (!checkCurrentChild && item == currItem) {
				doSearch = true;
			}
		}
		return null;
	}

	public boolean showTime() {
		return showTime;
	}

	public void setShowTime(boolean showTime) {
		this.showTime = showTime;
		getTreeViewer().refresh();
	}
	
	public void setShowFailedOnly(boolean showFailedOnly) {
		if (failedOnlyFilter == null) {
			failedOnlyFilter = new FailedOnlyFilter();
		}
		if (showFailedOnly) {
			getTreeViewer().addFilter(failedOnlyFilter);
		} else {
			getTreeViewer().removeFilter(failedOnlyFilter);
		}
	}

	public boolean showTestsHierarchy() {
		return showTestsHierarchy;
	}

	public void setShowTestsHierarchy(boolean showTestsHierarchy) {
		this.showTestsHierarchy = showTestsHierarchy;
		getTreeViewer().refresh();
	}

}

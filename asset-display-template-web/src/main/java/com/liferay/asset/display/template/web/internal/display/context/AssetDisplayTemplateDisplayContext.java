/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.asset.display.template.web.internal.display.context;

import com.liferay.asset.display.template.constants.AssetDisplayTemplatePortletKeys;
import com.liferay.asset.display.template.model.AssetDisplayTemplate;
import com.liferay.asset.display.template.service.AssetDisplayTemplateLocalServiceUtil;
import com.liferay.asset.display.template.util.comparator.AssetDisplayTemplateClassNameIdComparator;
import com.liferay.asset.display.template.util.comparator.AssetDisplayTemplateCreateDateComparator;
import com.liferay.asset.kernel.AssetRendererFactoryRegistryUtil;
import com.liferay.dynamic.data.mapping.util.DDMDisplayRegistry;
import com.liferay.dynamic.data.mapping.util.DDMTemplateHelper;
import com.liferay.portal.kernel.dao.search.EmptyOnClickRowChecker;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.portlet.PortalPreferences;
import com.liferay.portal.kernel.portlet.PortletPreferencesFactoryUtil;
import com.liferay.portal.kernel.theme.ThemeDisplay;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;

import java.util.List;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Pavel Savinov
 */
public class AssetDisplayTemplateDisplayContext {

	public AssetDisplayTemplateDisplayContext(
		RenderRequest renderRequest, RenderResponse renderResponse,
		HttpServletRequest request, DDMDisplayRegistry ddmDisplayRegistry,
		DDMTemplateHelper ddmTemplateHelper) {

		_renderRequest = renderRequest;
		_renderResponse = renderResponse;
		_request = request;

		_ddmDisplayRegistry = ddmDisplayRegistry;
		_ddmTemplateHelper = ddmTemplateHelper;
	}

	public long[] getAvailableClassNameIds() {
		if (_availableClassNameIds == null) {
			ThemeDisplay themeDisplay = (ThemeDisplay)_request.getAttribute(
				WebKeys.THEME_DISPLAY);

			_availableClassNameIds =
				AssetRendererFactoryRegistryUtil.getClassNameIds(
					themeDisplay.getCompanyId(), true);
		}

		return _availableClassNameIds;
	}

	public String getDisplayStyle() {
		if (Validator.isNotNull(_displayStyle)) {
			return _displayStyle;
		}

		PortalPreferences portalPreferences =
			PortletPreferencesFactoryUtil.getPortalPreferences(_request);

		_displayStyle = portalPreferences.getValue(
			AssetDisplayTemplatePortletKeys.ASSET_DISPLAY_TEMPLATE,
			"display-style", "list");

		return _displayStyle;
	}

	public String getKeywords() {
		if (Validator.isNotNull(_keywords)) {
			return _keywords;
		}

		_keywords = ParamUtil.getString(_request, "keywords", null);

		return _keywords;
	}

	public String getOrderByCol() {
		if (Validator.isNotNull(_orderByCol)) {
			return _orderByCol;
		}

		_orderByCol = ParamUtil.getString(
			_request, "orderByCol", "create-date");

		return _orderByCol;
	}

	public String getOrderByType() {
		if (Validator.isNotNull(_orderByType)) {
			return _orderByType;
		}

		_orderByType = ParamUtil.getString(_request, "orderByType", "asc");

		return _orderByType;
	}

	public SearchContainer getSearchContainer() throws PortalException {
		if (_searchContainer != null) {
			return _searchContainer;
		}

		ThemeDisplay themeDisplay = (ThemeDisplay)_request.getAttribute(
			WebKeys.THEME_DISPLAY);

		SearchContainer searchContainer = new SearchContainer(
			_renderRequest, _renderResponse.createRenderURL(), null,
			"there-are-no-asset-display-templates");

		if (!isShowSearch()) {
			if (isShowAddButton()) {
				searchContainer.setEmptyResultsMessage(
					"there-are-no-asset-display-templates-you-can-add-an-" +
						"asset-display-template-by-clicking-plus-button-on-" +
							"the-bottom-right-corner");
				searchContainer.setEmptyResultsMessageCssClass(
					"taglib-empty-result-message-header-has-plus-btn");
			}
		}
		else {
			searchContainer.setSearch(true);
		}

		searchContainer.setRowChecker(
			new EmptyOnClickRowChecker(_renderResponse));
		searchContainer.setOrderByCol(getOrderByCol());

		OrderByComparator<AssetDisplayTemplate> orderByComparator =
			_getOrderByComparator(getOrderByCol(), getOrderByType());

		searchContainer.setOrderByComparator(orderByComparator);

		searchContainer.setOrderByType(getOrderByType());

		int assetDisplayTemplatesCount =
			AssetDisplayTemplateLocalServiceUtil.getAssetDisplayTemplatesCount(
				themeDisplay.getScopeGroupId());

		searchContainer.setTotal(assetDisplayTemplatesCount);

		List<AssetDisplayTemplate> assetDisplayTemplates =
			AssetDisplayTemplateLocalServiceUtil.getAssetDisplayTemplates(
				themeDisplay.getScopeGroupId(), searchContainer.getStart(),
				searchContainer.getEnd(), orderByComparator);

		searchContainer.setResults(assetDisplayTemplates);

		_searchContainer = searchContainer;

		return _searchContainer;
	}

	public boolean isDisabledManagementBar() throws PortalException {
		SearchContainer searchContainer = getSearchContainer();

		if (searchContainer.getTotal() <= 0) {
			return true;
		}

		return false;
	}

	public boolean isShowAddButton() {
		return true;
	}

	public boolean isShowSearch() {
		if (Validator.isNotNull(getKeywords())) {
			return true;
		}

		return false;
	}

	private OrderByComparator<AssetDisplayTemplate> _getOrderByComparator(
		String orderByCol, String orderByType) {

		boolean orderByAsc = false;

		if (orderByType.equals("asc")) {
			orderByAsc = true;
		}

		OrderByComparator<AssetDisplayTemplate> orderByComparator = null;

		if (orderByCol.equals("create-date")) {
			orderByComparator = new AssetDisplayTemplateCreateDateComparator(
				orderByAsc);
		}
		else if (orderByCol.equals("asset-type")) {
			orderByComparator = new AssetDisplayTemplateClassNameIdComparator(
				orderByAsc);
		}

		return orderByComparator;
	}

	private long[] _availableClassNameIds;
	private final DDMDisplayRegistry _ddmDisplayRegistry;
	private final DDMTemplateHelper _ddmTemplateHelper;
	private String _displayStyle;
	private String _keywords;
	private String _orderByCol;
	private String _orderByType;
	private final RenderRequest _renderRequest;
	private final RenderResponse _renderResponse;
	private final HttpServletRequest _request;
	private SearchContainer _searchContainer;

}
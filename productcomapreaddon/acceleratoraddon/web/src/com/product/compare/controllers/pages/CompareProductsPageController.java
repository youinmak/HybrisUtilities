package com.product.compare.controllers.pages;

import de.hybris.platform.acceleratorfacades.device.DeviceDetectionFacade;
import de.hybris.platform.acceleratorfacades.device.data.DeviceData;
import de.hybris.platform.acceleratorstorefrontcommons.breadcrumb.ResourceBreadcrumbBuilder;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractSearchPageController;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.product.ProductFacade;
import de.hybris.platform.commercefacades.product.ProductOption;
import de.hybris.platform.commercefacades.product.data.ProductData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.product.compare.controllers.ProductcomapreaddonControllerConstants;



/**
 * Controller for CompareProductsPage
 */
@Controller
@RequestMapping("/productcomparison")
public class CompareProductsPageController extends AbstractSearchPageController
{
	@Resource(name = "productFacade")
	private ProductFacade productFacade;

	@Resource(name = "simpleBreadcrumbBuilder")
	private ResourceBreadcrumbBuilder simpleBreadcrumbBuilder;

	@Resource
	private DeviceDetectionFacade deviceDetectionFacade;

	private static final String PRODUCT_COMPARISON_PAGE_ID = "productComparisonPage";

	protected static final List<ProductOption> PRODUCT_OPTIONS = Arrays.asList(ProductOption.BASIC, ProductOption.PRICE,
			ProductOption.SUMMARY, ProductOption.REVIEW, ProductOption.CLASSIFICATION, ProductOption.DESCRIPTION,
			ProductOption.STOCK);

	@GetMapping()
	public String showCompareProducts(@RequestParam(required = false) final String p1,
			@RequestParam(required = false) final String p2, @RequestParam(required = false) final String p3,
			@RequestParam(required = false) final String p4, final Model model, final HttpServletRequest request)
			throws CMSItemNotFoundException
	{
		final List<String> productCodes = new ArrayList<String>();
		if (StringUtils.isNotEmpty(p1))
		{
			productCodes.add(p1);
		}
		if (StringUtils.isNotEmpty(p2))
		{
			productCodes.add(p2);
		}
		if (StringUtils.isNotEmpty(p3))
		{
			productCodes.add(p3);
		}
		if (StringUtils.isNotEmpty(p4))
		{
			productCodes.add(p4);
		}
		final List<ProductData> products = new ArrayList<>();

		for (final String productCode : productCodes)
		{
			products.add(productFacade.getProductForCodeAndOptions(productCode, PRODUCT_OPTIONS));
		}
		final HttpSession currentSession = request.getSession();

		model.addAttribute("compareProductInfo", products);
		model.addAttribute("breadcrumbs", simpleBreadcrumbBuilder.getBreadcrumbs("text.compareproduct"));

		if (null != currentSession)
		{
			currentSession.setAttribute("compareData", productCodes);
		}
		storeCmsPageInModel(model, getContentPageForLabelOrId(PRODUCT_COMPARISON_PAGE_ID));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(PRODUCT_COMPARISON_PAGE_ID));
		return ProductcomapreaddonControllerConstants.Views.Pages.PRODUCT_COMPARISON_PAGE;
	}


	@GetMapping(value = "/compare")
	public String compareProducts(final Model model, final HttpServletRequest request) throws CMSItemNotFoundException
	{
		final List<String> productCodes = new ArrayList<String>();
		final List<ProductData> products = new ArrayList<>();
		final HttpSession currentSession = request.getSession();
		if (null != currentSession && currentSession.getAttribute("compareData") != null)
		{
			productCodes.addAll((Collection<? extends String>) currentSession.getAttribute("compareData"));
		}

		for (final String productCode : productCodes)
		{
			products.add(productFacade.getProductForCodeAndOptions(productCode, PRODUCT_OPTIONS));
		}
		model.addAttribute("breadcrumbs", simpleBreadcrumbBuilder.getBreadcrumbs("text.compareproduct"));
		model.addAttribute("compareProductInfo", products);
		storeCmsPageInModel(model, getContentPageForLabelOrId(PRODUCT_COMPARISON_PAGE_ID));
		setUpMetaDataForContentPage(model, getContentPageForLabelOrId(PRODUCT_COMPARISON_PAGE_ID));
		return ProductcomapreaddonControllerConstants.Views.Pages.PRODUCT_COMPARISON_PAGE;
	}

	@ResponseBody
	@PostMapping(value = "/addToCompare", produces = "application/json")
	public List<ProductData> addToCompareProducts(@RequestParam(required = false) final String code,
			final HttpServletRequest request, final Model model) throws CMSItemNotFoundException
	{
		int productCompareSize = 4;
		final List<String> productCodes = new ArrayList<String>();
		final HttpSession currentSession = request.getSession();
		if (null != currentSession && currentSession.getAttribute("compareData") != null)
		{
			productCodes.addAll((Collection<? extends String>) currentSession.getAttribute("compareData"));
		}
		final DeviceData deviceData = deviceDetectionFacade.getCurrentDetectedDevice();
		final boolean isMobile = deviceData.getMobileBrowser().booleanValue();
		if (isMobile)
		{
			productCompareSize = 2;
		}

		if (productCodes.size() >= productCompareSize)
		{
			return null;
		}

		if (StringUtils.isNotEmpty(code) && !productCodes.contains(code))
		{
			productCodes.add(code);
		}
		final List<ProductData> products = new ArrayList<ProductData>();

		for (final String productCode : productCodes)
		{
			products.add(productFacade.getProductForCodeAndOptions(productCode, PRODUCT_OPTIONS));
		}
		if (null != currentSession)
		{
			currentSession.setAttribute("compareData", productCodes);
		}
		model.addAttribute("compareProductInfo", products);
		return products;
	}

	@ResponseBody
	@PostMapping(value = "/removeCompareProduct", produces = "application/json")
	public List<ProductData> removeCompareProduct(@RequestParam(required = false) final String code,
			final HttpServletRequest request, final Model model) throws CMSItemNotFoundException
	{
		final List<String> productCodes = new ArrayList<String>();

		final HttpSession currentSession = request.getSession();
		if (null != currentSession && currentSession.getAttribute("compareData") != null)
		{
			productCodes.addAll((Collection<? extends String>) currentSession.getAttribute("compareData"));
		}

		if (StringUtils.isNotEmpty(code) && productCodes.contains(code))
		{
			productCodes.remove(code);
		}
		final List<ProductData> products = new ArrayList<ProductData>();

		for (final String productCode : productCodes)
		{
			products.add(productFacade.getProductForCodeAndOptions(productCode, PRODUCT_OPTIONS));
		}
		if (null != currentSession)
		{
			currentSession.setAttribute("compareData", productCodes);
		}
		model.addAttribute("compareProductInfo", products);
		return products;
	}
}
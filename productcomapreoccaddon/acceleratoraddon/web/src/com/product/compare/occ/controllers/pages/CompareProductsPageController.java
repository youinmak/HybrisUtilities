package com.product.compare.occ.controllers.pages;

import de.hybris.platform.acceleratorfacades.device.DeviceDetectionFacade;
import de.hybris.platform.acceleratorfacades.device.data.DeviceData;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.product.ProductFacade;
import de.hybris.platform.commercefacades.product.ProductOption;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commerceservices.request.mapping.annotation.ApiVersion;
import de.hybris.platform.commercewebservicescommons.dto.product.ProductListWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.product.ProductWsDTO;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;




/**
 * Controller for CompareProductsPage
 */
@RestController
@RequestMapping("/{baseSiteId}/productcomparison")
@ApiVersion("v2")
public class CompareProductsPageController
{
	@Resource(name = "productFacade")
	private ProductFacade productFacade;

	@Resource(name = "dataMapper")
	private DataMapper dataMapper;

	@Resource
	private DeviceDetectionFacade deviceDetectionFacade;

	protected static final List<ProductOption> PRODUCT_OPTIONS = Arrays.asList(ProductOption.BASIC, ProductOption.PRICE,
			ProductOption.SUMMARY, ProductOption.REVIEW, ProductOption.CLASSIFICATION, ProductOption.DESCRIPTION,
			ProductOption.STOCK);

	@GetMapping()
	public ProductListWsDTO showCompareProducts(@RequestParam(required = false) final String p1,
			@RequestParam(required = false) final String p2, @RequestParam(required = false) final String p3,
			@RequestParam(required = false) final String p4,
			@RequestParam(defaultValue = FieldSetLevelHelper.DEFAULT_LEVEL) final String fields, final HttpServletRequest request)
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

		if (null != currentSession)
		{
			currentSession.setAttribute("compareData", productCodes);
		}

		final ProductListWsDTO productListWsDTO = new ProductListWsDTO();

		productListWsDTO.setProducts(dataMapper.mapAsList(products, ProductWsDTO.class, fields));

		return productListWsDTO;
	}


	@GetMapping(value = "/compare")
	public ProductListWsDTO compareProducts(final HttpServletRequest request,
			@RequestParam(defaultValue = FieldSetLevelHelper.DEFAULT_LEVEL) final String fields)
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

		final ProductListWsDTO productListWsDTO = new ProductListWsDTO();

		productListWsDTO.setProducts(dataMapper.mapAsList(products, ProductWsDTO.class, fields));

		return productListWsDTO;

	}

	@PostMapping(value = "/addToCompare", produces = "application/json")
	public ProductListWsDTO addToCompareProducts(@RequestParam(required = false) final String code,
			final HttpServletRequest request, @RequestParam(defaultValue = FieldSetLevelHelper.DEFAULT_LEVEL) final String fields)
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
			return new ProductListWsDTO();
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

		final ProductListWsDTO productListWsDTO = new ProductListWsDTO();

		productListWsDTO.setProducts(dataMapper.mapAsList(products, ProductWsDTO.class, fields));

		return productListWsDTO;
	}

	@PostMapping(value = "/removeCompareProduct", produces = "application/json")
	public ProductListWsDTO removeCompareProduct(@RequestParam(required = false) final String code,
			final HttpServletRequest request, @RequestParam(defaultValue = FieldSetLevelHelper.DEFAULT_LEVEL) final String fields)
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

		final ProductListWsDTO productListWsDTO = new ProductListWsDTO();

		productListWsDTO.setProducts(dataMapper.mapAsList(products, ProductWsDTO.class, fields));

		return productListWsDTO;

	}
}
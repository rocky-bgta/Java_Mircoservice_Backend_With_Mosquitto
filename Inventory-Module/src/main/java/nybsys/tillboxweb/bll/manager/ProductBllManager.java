/**
 * Created By: Md. Abdul Hannan
 * Created Date: 2/14/2018
 * Time: 10:53 AM
 * Modified By:
 * Modified date:
 * (C) CopyRight NybSys ltd.
 */
package nybsys.tillboxweb.bll.manager;

import nybsys.tillboxweb.BaseBll;
import nybsys.tillboxweb.Core;
import nybsys.tillboxweb.appenum.TillBoxAppEnum;
import nybsys.tillboxweb.constant.MessageConstant;
import nybsys.tillboxweb.constant.TillBoxAppConstant;
import nybsys.tillboxweb.coreEnum.ReferenceType;
import nybsys.tillboxweb.coreModels.InventoryTransactionModel;
import nybsys.tillboxweb.coreModels.ProductCategoryModel;
import nybsys.tillboxweb.entities.PriceCategory;
import nybsys.tillboxweb.entities.Product;
import nybsys.tillboxweb.entities.ProductPicture;
import nybsys.tillboxweb.models.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ProductBllManager extends BaseBll<Product> {
    private static final Logger log = LoggerFactory.getLogger(ProductBllManager.class);

    private ProductPurchasePriceBllManager productPurchasePriceBllManager = new ProductPurchasePriceBllManager();
    private ProductSalesPriceBllManager productSalesPriceBllManager = new ProductSalesPriceBllManager();
    private ProductAttributeMapperBllManger productAttributeMapperBllManger = new ProductAttributeMapperBllManger();
    private InventoryTransactionBllManager inventoryTransactionBllManager;// = new InventoryTransactionBllManager();
    private ProductCategoryBllManager productCategoryBllManager = new ProductCategoryBllManager();
    private ProductPriceListBllManager productPriceListBllManager;

    private ProductPictureBllManager productPictureBllManager;

    @Override
    protected void initEntityModel() {
        Core.runTimeModelType.remove();
        Core.runTimeEntityType.remove();
        Core.runTimeEntityType.set(Product.class);
        Core.runTimeModelType.set(ProductModel.class);
    }

    public VMProduct saveProduct(VMProduct vmProduct, int businessID) throws Exception {
        try {
            this.inventoryTransactionBllManager = new InventoryTransactionBllManager();
            this.productPictureBllManager = new ProductPictureBllManager();
            vmProduct.productModel.setBusinessID(businessID);
            if (isValidProduct(vmProduct)) {

                if (vmProduct.productModel.getProductID() != null && vmProduct.productModel.getProductID() > 0) {

                    vmProduct.productModel = this.update(vmProduct.productModel);

                    ProductPurchasePriceModel searchProductPurchasePrice = new ProductPurchasePriceModel();
                    List<ProductPurchasePriceModel> productPurchasePriceModels = new ArrayList<>();
                    searchProductPurchasePrice.setProductID(vmProduct.productModel.getProductID());
                    productPurchasePriceModels = this.productPurchasePriceBllManager.getAllByConditions(searchProductPurchasePrice);

                    for (ProductPurchasePriceModel productPurchasePriceModel : productPurchasePriceModels) {
                        productPurchasePriceModel.setStatus(TillBoxAppEnum.Status.Deleted.get());
                        this.productPurchasePriceBllManager.update(productPurchasePriceModel);
                    }

                    ProductSalesPriceModel searchProductSalesPriceModel = new ProductSalesPriceModel();
                    searchProductSalesPriceModel.setProductID(vmProduct.productModel.getProductID());
                    List<ProductSalesPriceModel> productSalesPriceModels = new ArrayList<>();
                    productSalesPriceModels = this.productSalesPriceBllManager.getAllByConditions(searchProductSalesPriceModel);
                    for (ProductSalesPriceModel productSalesPriceModel : productSalesPriceModels) {
                        productSalesPriceModel.setStatus(TillBoxAppEnum.Status.Deleted.get());
                        this.productSalesPriceBllManager.update(productSalesPriceModel);
                    }

                    ProductAttributeMapperModel searchProductAttributeMapperModel = new ProductAttributeMapperModel();
                    searchProductAttributeMapperModel.setProductID(vmProduct.productModel.getProductID());
                    List<ProductAttributeMapperModel> productAttributeMapperModels = new ArrayList<>();
                    productAttributeMapperModels = this.productAttributeMapperBllManger.getAllByConditions(searchProductAttributeMapperModel);

                    for (ProductAttributeMapperModel productAttributeMapperModel : productAttributeMapperModels) {
                        productAttributeMapperModel.setStatus(TillBoxAppEnum.Status.Deleted.get());
                        this.productAttributeMapperBllManger.update(productAttributeMapperModel);
                    }

                    nybsys.tillboxweb.coreModels.InventoryTransactionModel searchInventoryTransaction = new InventoryTransactionModel();
                    searchInventoryTransaction.setReferenceID(vmProduct.productModel.getProductID());
                    searchInventoryTransaction.setReferenceType(ReferenceType.Product.get());
                    nybsys.tillboxweb.coreModels.InventoryTransactionModel invTransModel = new InventoryTransactionModel();

                    List<nybsys.tillboxweb.coreModels.InventoryTransactionModel> inventoryTransactionModels = this.inventoryTransactionBllManager.getAllByConditions(searchInventoryTransaction);
                    if (inventoryTransactionModels.size() > 0) {
                        invTransModel = inventoryTransactionModels.get(0);
                        if (invTransModel != null) {
                            invTransModel.setStatus(TillBoxAppEnum.Status.Deleted.get());
                            this.inventoryTransactionBllManager.update(invTransModel);
                        }
                    }

                    ProductPictureModel searchProductPicture = new ProductPictureModel();
                    searchProductPicture.setProductID(vmProduct.productModel.getProductID());
                    searchProductPicture.setBusinessID(businessID);

                    List<ProductPictureModel> productPictureList = new ArrayList<>();
                    productPictureList = this.productPictureBllManager.getAllByConditionWithActive(searchProductPicture);
                    for (ProductPictureModel productPictureModel : productPictureList) {
                        productPictureModel.setStatus(TillBoxAppEnum.Status.Deleted.get());
                        this.productPictureBllManager.update(productPictureModel);
                    }

                    if (vmProduct.productPicture.getPicture() != null) {
                        vmProduct.productPicture.setProductID(vmProduct.productModel.getProductID());
                        vmProduct.productPicture.setBusinessID(businessID);
                        vmProduct.productPicture = this.productPictureBllManager.save(vmProduct.productPicture);
                    }
//                if (vmProduct.productDocumentModel.getDocument() != null) {
//                    vmProduct.productDocumentModel.setProductID(vmProduct.productModel.getProductID());
//                    vmProduct.productDocumentModel = this.productDocumentBllManager.save(vmProduct.productDocumentModel);
//                }

                    for (ProductPurchasePriceModel productPurchasePriceModel : vmProduct.lstProductPurchasePriceModel) {
                        productPurchasePriceModel.setBusinessID(businessID);
                        productPurchasePriceModel.setProductID(vmProduct.productModel.getProductID());
                        productPurchasePriceModel = this.productPurchasePriceBllManager.saveProductPurchasePrice(productPurchasePriceModel, Core.clientMessage.get());
                    }

                    for (ProductSalesPriceModel productSalesPriceModel : vmProduct.lstProductSalesPriceModel) {
                        productSalesPriceModel.setBusinessID(businessID);
                        productSalesPriceModel.setProductID(vmProduct.productModel.getProductID());
                        productSalesPriceModel = this.productSalesPriceBllManager.saveProductSalesPriceModel(productSalesPriceModel, Core.clientMessage.get());
                    }
                    for (ProductAttributeMapperModel productAttributeMapperModel : vmProduct.lstProductAttributeMapperModels) {
                        productAttributeMapperModel.setBusinessID(businessID);
                        productAttributeMapperModel.setProductID(vmProduct.productModel.getProductID());
                        productAttributeMapperModel = this.productAttributeMapperBllManger.saveProductAttributeMapper(productAttributeMapperModel);
                    }


                    if (vmProduct.productModel.getOpeningQuantity() != null && vmProduct.productModel.getOpeningQuantity() > 0) {

                        nybsys.tillboxweb.coreModels.InventoryTransactionModel inventoryTransactionModel = new InventoryTransactionModel();
                        inventoryTransactionModel.setBusinessID(vmProduct.productModel.getBusinessID());
                        inventoryTransactionModel.setProductID(vmProduct.productModel.getProductID());
                        inventoryTransactionModel.setInQuantity(vmProduct.productModel.getOpeningQuantity());
                        inventoryTransactionModel.setReferenceID(vmProduct.productModel.getProductID());
                        inventoryTransactionModel.setReferenceType(ReferenceType.Product.get());
                        inventoryTransactionModel.setPrice(vmProduct.productModel.getOpeningCost());
                        inventoryTransactionModel.setBusinessID(businessID);
                        this.inventoryTransactionBllManager.save(inventoryTransactionModel);
                    }


                } else {
                    vmProduct.productModel = this.saveWithStatus(vmProduct.productModel);

//                if (vmProduct.productPictureModel.getPicture() != null) {
//                    vmProduct.productPictureModel.setProductID(vmProduct.productModel.getProductID());
//                    vmProduct.productPictureModel = this.productPictureBllManager.save(vmProduct.productPictureModel);
//                }
//                if (vmProduct.productDocumentModel.getDocument() != null) {
//                    vmProduct.productDocumentModel.setProductID(vmProduct.productModel.getProductID());
//                    vmProduct.productDocumentModel = this.productDocumentBllManager.save(vmProduct.productDocumentModel);
//                }

                    for (ProductPurchasePriceModel productPurchasePriceModel : vmProduct.lstProductPurchasePriceModel) {
                        productPurchasePriceModel.setBusinessID(businessID);
                        productPurchasePriceModel.setProductID(vmProduct.productModel.getProductID());
                        productPurchasePriceModel = this.productPurchasePriceBllManager.saveProductPurchasePrice(productPurchasePriceModel, Core.clientMessage.get());
                    }

                    for (ProductSalesPriceModel productSalesPriceModel : vmProduct.lstProductSalesPriceModel) {
                        productSalesPriceModel.setBusinessID(businessID);
                        productSalesPriceModel.setProductID(vmProduct.productModel.getProductID());
                        productSalesPriceModel = this.productSalesPriceBllManager.saveProductSalesPriceModel(productSalesPriceModel, Core.clientMessage.get());
                    }
                    for (ProductAttributeMapperModel productAttributeMapperModel : vmProduct.lstProductAttributeMapperModels) {
                        productAttributeMapperModel.setBusinessID(businessID);
                        productAttributeMapperModel.setProductID(vmProduct.productModel.getProductID());
                        productAttributeMapperModel = this.productAttributeMapperBllManger.saveProductAttributeMapper(productAttributeMapperModel);
                    }

                    if (vmProduct.productPicture.getPicture() != null) {
                        vmProduct.productPicture.setProductID(vmProduct.productModel.getProductID());
                        vmProduct.productPicture.setBusinessID(businessID);
                        vmProduct.productPicture = this.productPictureBllManager.save(vmProduct.productPicture);
                    }


                    if (vmProduct.productModel.getOpeningQuantity() != null && vmProduct.productModel.getOpeningQuantity() > 0) {
                        nybsys.tillboxweb.coreModels.InventoryTransactionModel inventoryTransactionModel = new InventoryTransactionModel();
                        inventoryTransactionModel.setBusinessID(vmProduct.productModel.getBusinessID());
                        inventoryTransactionModel.setProductID(vmProduct.productModel.getProductID());
                        inventoryTransactionModel.setInQuantity(vmProduct.productModel.getOpeningQuantity());
                        inventoryTransactionModel.setReferenceID(vmProduct.productModel.getProductID());
                        inventoryTransactionModel.setReferenceType(ReferenceType.Product.get());
                        inventoryTransactionModel.setPrice(vmProduct.productModel.getOpeningCost());
                        inventoryTransactionModel.setBusinessID(businessID);
                        this.inventoryTransactionBllManager.save(inventoryTransactionModel);
                    }

                }
            }


//            if ( Core.clientMessage.get().messageCode == null) {
//                Core.clientMessage.get().message = MessageConstant.SUCCESSFULLY_SAVE_PRODUCT_ATTRIBUTE;
//            } else {
//
//                Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
//                Core.clientMessage.get().message = MessageConstant.FAILED_TO_SAVE_PRODUCT_ATTRIBUTE;
//            }
            if (vmProduct.productModel == null) {
                Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                Core.clientMessage.get().message = MessageConstant.FAILED_TO_SAVE_PRODUCT_ATTRIBUTE;
            }

        } catch (Exception ex) {

            log.error("Error from save product (Service Manager) : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return vmProduct;
    }

    public List<ProductModel> getActiveAndInactiveProducts(Integer businessID) throws Exception {
        List<ProductModel> lstProductModel = new ArrayList<>();
        try {
            String hql = "From Product p where p.businessID = " + businessID + " and p.status != " + TillBoxAppEnum.Status.Deleted.get();
            lstProductModel = this.executeHqlQuery(hql, ProductModel.class, TillBoxAppEnum.QueryType.Select.get());
        } catch (Exception ex) {

            log.error("ProductTypeBllManager -> getActiveAndInactiveProduct got exception: " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return lstProductModel;
    }

    private Boolean isValidProduct(VMProduct vmProduct) throws Exception {
        ProductModel existingProductModel = new ProductModel();
        existingProductModel.setBusinessID(vmProduct.productModel.getBusinessID());
        existingProductModel.setCode(vmProduct.productModel.getCode());
        List<ProductModel> lstProductModel = new ArrayList<>();

        ProductAttributeMapperModel productAttributeMapperModel = new ProductAttributeMapperModel();

        List<ProductAttributeMapperModel> lstExistingProductAttributeMapperModel = new ArrayList<>();

        lstProductModel = this.getAllByConditions(existingProductModel);

        if (vmProduct.productModel.getProductID() != null && vmProduct.productModel.getProductID() > 0) {
            existingProductModel = this.getById(vmProduct.productModel.getProductID(), TillBoxAppEnum.Status.Active.get());
        } else {
            lstProductModel = this.getAllByConditions(existingProductModel);
        }
        if (lstProductModel.size() > 0) {
            existingProductModel = lstProductModel.get(0);
            productAttributeMapperModel.setProductID(existingProductModel.getProductID());
            lstExistingProductAttributeMapperModel = this.productAttributeMapperBllManger.getAllByConditions(productAttributeMapperModel);

        }


//        Collections.sort(lstExistingProductAttributeMapperModel, Collections.reverseOrder(a-> a.g));


        Comparator<ProductAttributeMapperModel> nameShorted = (o1, o2) -> o1.getProductAttributeID().compareTo(o2.getProductAttributeID());
        lstExistingProductAttributeMapperModel = lstExistingProductAttributeMapperModel.stream().sorted(nameShorted).collect(Collectors.toList());

        vmProduct.lstProductAttributeMapperModels = vmProduct.lstProductAttributeMapperModels.stream().sorted(nameShorted).collect(Collectors.toList());

        if (vmProduct.lstProductAttributeMapperModels.size() > 0) {
            String newMapperCombination = "";
            String existingMapperCombination = "";

            boolean duplicate = false;
            for (ProductAttributeMapperModel pAttributeMapperModel : vmProduct.lstProductAttributeMapperModels) {
                newMapperCombination = new StringBuilder().append(newMapperCombination).append(pAttributeMapperModel.getProductAttributeID()).append(pAttributeMapperModel.getProductAttributeValueID()).toString();
            }

            for (ProductAttributeMapperModel pAttributeMapperModel : lstExistingProductAttributeMapperModel) {
                existingMapperCombination = new StringBuilder().append(existingMapperCombination).append(pAttributeMapperModel.getProductAttributeID()).append(pAttributeMapperModel.getProductAttributeValueID()).toString();
            }


            if ((existingProductModel.getProductID() != null && existingProductModel.getProductID() > 0) && existingProductModel.getProductID().intValue() != vmProduct.productModel.getProductID().intValue()) {

                if (StringUtils.equals(newMapperCombination, existingMapperCombination)) {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    Core.clientMessage.get().userMessage = MessageConstant.PRODUCT_NAME_ALREADY_EXISTS;
                    return false;
                }
            }
        } else {
            if ((existingProductModel != null && existingProductModel.getProductID() != null) && existingProductModel.getProductID() != vmProduct.productModel.getProductID()) {
                Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                Core.clientMessage.get().userMessage = MessageConstant.PRODUCT_CODE_ALREADY_EXISTS;
                return false;
            }
        }


        return true;
    }

    public List<VMProduct> getFilteredVMProduct(ProductModel productModel) throws Exception {
        this.productPictureBllManager = new ProductPictureBllManager();
        List<VMProduct> lstVMProduct = new ArrayList<>();

        List<ProductModel> lstProductModel = new ArrayList<>();
        lstProductModel = this.getAllByConditions(productModel);
        for (ProductModel pModel : lstProductModel) {
            VMProduct vmProduct = new VMProduct();
            vmProduct.productModel = pModel;
            ProductPurchasePriceModel productPurchasePriceModel = new ProductPurchasePriceModel();
            ProductSalesPriceModel productSalesPriceModel = new ProductSalesPriceModel();
            ProductPictureModel productPictureModel = new ProductPictureModel();
            ProductAttributeMapperModel productAttributeMapperModel = new ProductAttributeMapperModel();
            ProductDocumentModel productDocumentModel = new ProductDocumentModel();

            productPurchasePriceModel.setProductID(pModel.getProductID());
            productDocumentModel.setProductID(pModel.getProductID());
            productSalesPriceModel.setProductID(pModel.getProductID());

            productAttributeMapperModel.setProductID(pModel.getProductID());

            vmProduct.lstProductPurchasePriceModel = this.productPurchasePriceBllManager.getAllByConditionWithActive(productPurchasePriceModel);
            vmProduct.lstProductSalesPriceModel = this.productSalesPriceBllManager.getAllByConditionWithActive(productSalesPriceModel);
            vmProduct.lstProductAttributeMapperModels = this.productAttributeMapperBllManger.getAllByConditionWithActive(productAttributeMapperModel);
            List<ProductPictureModel> productPictureModels = new ArrayList<>();

            productPictureModel.setProductID(pModel.getProductID());
            productPictureModel.setBusinessID(pModel.getBusinessID());
            productPictureModels = this.productPictureBllManager.getAllByConditions(productPictureModel);
            if (productPictureModels.size() > 0) {
                vmProduct.productPicture = productPictureModels.get(0);
            }

//            vmProduct.productPictureModel = (this.productPictureBllManager.getAllByConditions(productPictureModel).size() > 0) ?
//                    this.productPictureBllManager.getAllByConditions(productPictureModel).get(0) : new ProductPictureModel();
//
//            vmProduct.productDocumentModel = (this.productDocumentBllManager.getAllByConditions(productDocumentModel).size() > 0) ?
//                    this.productDocumentBllManager.getAllByConditions(productDocumentModel).get((0)) : new ProductDocumentModel();

            lstVMProduct.add(vmProduct);
        }


        return lstVMProduct;
    }

    public List<VMProduct> getLikeFilteredProduct(ProductModel productModel, Integer businessID) throws Exception {

        List<VMProduct> lstVMProduct = new ArrayList<>();
        try {
            List<ProductModel> lstProductModel = new ArrayList<>();
            String hql = "FROM Product P WHERE P.status = " + TillBoxAppEnum.Status.Active.get() + " AND P.businessID = " + businessID + " AND P.name LIKE '%" + productModel.getName() + "%'";
            lstProductModel = this.executeHqlQuery(hql, ProductModel.class, TillBoxAppEnum.QueryType.Join.get());
            for (ProductModel pModel : lstProductModel) {
                VMProduct vmProduct = new VMProduct();
                vmProduct.productModel = pModel;
                ProductPurchasePriceModel productPurchasePriceModel = new ProductPurchasePriceModel();
                ProductSalesPriceModel productSalesPriceModel = new ProductSalesPriceModel();
                ProductPictureModel productPictureModel = new ProductPictureModel();
                ProductAttributeMapperModel productAttributeMapperModel = new ProductAttributeMapperModel();
                ProductDocumentModel productDocumentModel = new ProductDocumentModel();

                productPurchasePriceModel.setProductID(pModel.getProductID());
                productDocumentModel.setProductID(pModel.getProductID());
                productSalesPriceModel.setProductID(pModel.getProductID());
                productPictureModel.setProductID(pModel.getProductID());
                productAttributeMapperModel.setProductID(pModel.getProductID());

                vmProduct.lstProductPurchasePriceModel = this.productPurchasePriceBllManager.getAllByConditions(productPurchasePriceModel);
                vmProduct.lstProductSalesPriceModel = this.productSalesPriceBllManager.getAllByConditions(productSalesPriceModel);
                vmProduct.lstProductAttributeMapperModels = this.productAttributeMapperBllManger.getAllByConditions(productAttributeMapperModel);

//            vmProduct.productPictureModel = (this.productPictureBllManager.getAllByConditions(productPictureModel).size() > 0) ?
//                    this.productPictureBllManager.getAllByConditions(productPictureModel).get(0) : new ProductPictureModel();
//
//            vmProduct.productDocumentModel = (this.productDocumentBllManager.getAllByConditions(productDocumentModel).size() > 0) ?
//                    this.productDocumentBllManager.getAllByConditions(productDocumentModel).get((0)) : new ProductDocumentModel();

                lstVMProduct.add(vmProduct);
            }
            if (lstProductModel.size() == 0) {
                Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                Core.clientMessage.get().message = MessageConstant.PRODUCT_GET_FAILED;

            }

        } catch (Exception ex) {
            log.error("ProductBllManager -> getLikeFilteredProduct got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return lstVMProduct;
    }

    public VMProduct getProductByID(ProductModel productModel) throws Exception {

        ProductModel pModel = new ProductModel();
        pModel = this.getById(productModel.getProductID());

        VMProduct vmProduct = new VMProduct();
        vmProduct.productModel = pModel;
        ProductPurchasePriceModel productPurchasePriceModel = new ProductPurchasePriceModel();
        ProductSalesPriceModel productSalesPriceModel = new ProductSalesPriceModel();
        ProductPictureModel productPictureModel = new ProductPictureModel();
        ProductAttributeMapperModel productAttributeMapperModel = new ProductAttributeMapperModel();
        ProductDocumentModel productDocumentModel = new ProductDocumentModel();

        productPurchasePriceModel.setProductID(pModel.getProductID());
        productDocumentModel.setProductID(pModel.getProductID());
        productSalesPriceModel.setProductID(pModel.getProductID());
        productPictureModel.setProductID(pModel.getProductID());
        productAttributeMapperModel.setProductID(pModel.getProductID());

        vmProduct.lstProductPurchasePriceModel = this.productPurchasePriceBllManager.getAllByConditionWithActive(productPurchasePriceModel);
        vmProduct.lstProductSalesPriceModel = this.productSalesPriceBllManager.getAllByConditionWithActive(productSalesPriceModel);
        vmProduct.lstProductAttributeMapperModels = this.productAttributeMapperBllManger.getAllByConditionWithActive(productAttributeMapperModel);

//        vmProduct.productPictureModel = (this.productPictureBllManager.getAllByConditions(productPictureModel).size() > 0) ?
//                this.productPictureBllManager.getAllByConditions(productPictureModel).get(0) : new ProductPictureModel();
//
//        vmProduct.productDocumentModel = (this.productDocumentBllManager.getAllByConditions(productDocumentModel).size() > 0) ?
//                this.productDocumentBllManager.getAllByConditions(productDocumentModel).get((0)) : new ProductDocumentModel();


        return vmProduct;
    }

    public List<VMProductWithStockAndPrice> getProductWithStockAndPrice(ProductModel productModelReq) throws Exception {
        List<VMProductWithStockAndPrice> lstVmProductWithStockAndPrice = new ArrayList<>();
        ProductModel wheareCondition;
        List<ProductModel> lstProductModel;
        try {
            wheareCondition = productModelReq;
            if (wheareCondition.getStatus() == null) {
                wheareCondition.setStatus(TillBoxAppEnum.Status.Active.get());
            }
            lstProductModel = this.getAllByConditions(wheareCondition);
            if (lstProductModel.size() > 0) {
                for (ProductModel productItem : lstProductModel) {
                    VMProductWithStockAndPrice vmProductWithStockAndPrice = new VMProductWithStockAndPrice();
                    vmProductWithStockAndPrice.productModel = productItem;

                    ProductPurchasePriceModel productPurchasePriceModel = new ProductPurchasePriceModel();
                    productPurchasePriceModel = productPurchasePriceBllManager.getById(productItem.getProductID(), TillBoxAppEnum.Status.Active.get());
                    vmProductWithStockAndPrice.productPurchasePriceModel = productPurchasePriceModel;

                    ProductSalesPriceModel productSalesPriceModel = new ProductSalesPriceModel();
                    productSalesPriceModel = productSalesPriceBllManager.getById(productItem.getProductID(), TillBoxAppEnum.Status.Active.get());
                    vmProductWithStockAndPrice.productSalesPriceModel = productSalesPriceModel;

                    Double stock = 0.0;
                    stock = inventoryTransactionBllManager.getStock(productItem.getBusinessID(), productItem.getProductID());
                    vmProductWithStockAndPrice.stock = stock;
                    lstVmProductWithStockAndPrice.add(vmProductWithStockAndPrice);
                }
            }

        } catch (Exception ex) {
            log.error("ProductBllManager -> getProductWithStockAndPrice got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return lstVmProductWithStockAndPrice;
    }

    public void updateProductCode(VMReNumberItemCode vmReNumberItemCode) throws Exception {
        ProductModel productModel;
        try {
            for (int index = 0; index < vmReNumberItemCode.lstReNumberItemCodeModel.size(); index++) {
                productModel = this.getById(vmReNumberItemCode.lstReNumberItemCodeModel.get(index).getProductID(), TillBoxAppEnum.Status.Active.get());
                if (productModel == null) {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    Core.clientMessage.get().message = MessageConstant.PRODUCT_GET_FAILED;
                    return;
                }

                productModel.setCode(vmReNumberItemCode.lstReNumberItemCodeModel.get(index).getCode());
                productModel = this.update(productModel);
                if (productModel == null) {
                    Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                    Core.clientMessage.get().message = MessageConstant.PRODUCT_UPDATE_FAILED;
                    return;
                }
            }

        } catch (Exception ex) {
            log.error("ProductBllManager -> updateProductCode got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
    }

    public VMReNumberItemCode getProductCodeList(Integer businessID) throws Exception {
        VMReNumberItemCode vmReNumberItemCode = new VMReNumberItemCode();
        try {
            String hql = "SELECT p.productID,p.name,p.code FROM Product p WHERE p.status = " + TillBoxAppEnum.Status.Active.get() + " AND p.businessID = " + businessID;
            vmReNumberItemCode.lstReNumberItemCodeModel = this.executeHqlQuery(hql, ReNumberItemCodeModel.class, TillBoxAppEnum.QueryType.Join.get());

        } catch (Exception ex) {
            log.error("ProductBllManager -> getProductCodeList got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return vmReNumberItemCode;
    }

    public List<VMProductView> getAllProductView(ProductModel productModel) throws Exception {
        this.inventoryTransactionBllManager = new InventoryTransactionBllManager();
        List<VMProductView> lstVMProductView = new ArrayList<>();
        // List<ProductCategoryModel> productCategoryModels = new ArrayList<>();
//        List<ProductSalesPriceModel> productSalesPriceModels = new ArrayList<>();

        List<ProductModel> lstProductModel = new ArrayList<>();


        lstProductModel = this.getAllByConditions(productModel);

        try {
            for (ProductModel pModel : lstProductModel) {
                VMProductView vmProductView = new VMProductView();
                vmProductView.code = pModel.getCode();
                vmProductView.name = pModel.getName();
                vmProductView.productCategoryID = pModel.getProductCategoryID();
                vmProductView.uOMID = pModel.getuOMID();
                vmProductView.salesVATID = pModel.getSalesVATID();
                vmProductView.purchaseVATID = pModel.getPurchaseVATID();
                vmProductView.openingCost = pModel.getOpeningCost();
                vmProductView.openingQuantity = pModel.getOpeningQuantity();

                ProductPurchasePriceModel productPurchasePriceModel = new ProductPurchasePriceModel();
                ProductSalesPriceModel productSalesPriceModel = new ProductSalesPriceModel();
                ProductCategoryModel productCategoryModel = new ProductCategoryModel();

                InventoryTransactionModel searchInventoryTransactionModel = new InventoryTransactionModel();
                ProductSalesPriceModel searchProductSalesPriceModel = new ProductSalesPriceModel();
                ProductCategoryModel searchProductCategoryModel = new ProductCategoryModel();


                searchProductSalesPriceModel.setBusinessID(productModel.getBusinessID());
                searchProductSalesPriceModel.setProductID(pModel.getProductID());
                searchProductSalesPriceModel.setStatus(TillBoxAppEnum.Status.Active.get());
                searchProductCategoryModel.setProductCategoryID(pModel.getProductCategoryID());
                searchProductCategoryModel.setBusinessID(productModel.getBusinessID());

                List<ProductCategoryModel> productCategoryModels = new ArrayList<>();

                productCategoryModels = this.productCategoryBllManager.getAllByConditionWithActive(searchProductCategoryModel);
                if (productCategoryModels.size() > 0) {
                    productCategoryModel = productCategoryModels.get(0);
                }
                List<ProductSalesPriceModel> productSalesPriceModels = new ArrayList<>();
                productSalesPriceModels = this.productSalesPriceBllManager.getAllByConditionWithActive(searchProductSalesPriceModel);
                if (productSalesPriceModels.size() > 0) {
                    productSalesPriceModel = productSalesPriceModels.get(0);
                } else {
                    productSalesPriceModel = null;
                }

                vmProductView.productID = pModel.getProductID();
                vmProductView.productCategoryID = (pModel.getProductCategoryID() != null) ? pModel.getProductCategoryID() : 0;
                vmProductView.categoryName = (productCategoryModel.getName() != null) ? productCategoryModel.getName() : null;
                vmProductView.inclusiveSellingPrice = (productSalesPriceModel != null) ? productSalesPriceModel.getSalesPrice() : 0;
                vmProductView.exclusiveSellingPrice = (productSalesPriceModel != null) ? productSalesPriceModel.getExclusiveSalesPrice() : 0;

                double openingCost = (pModel.getOpeningCost() == null) ? 0 : pModel.getOpeningCost();

                double lastCost = calculateLastPrice(pModel.getProductID(), pModel.getBusinessID());
                vmProductView.lastCost = (lastCost > 0) ? lastCost : openingCost;

                double averageCost = calculateAveragePrice(pModel.getProductID(), pModel.getBusinessID());
                vmProductView.averageCost = (averageCost > 0) ? averageCost : openingCost;

                vmProductView.quantityOnHand = calculateQuantityOnHand(pModel.getProductID(), pModel.getBusinessID());
                vmProductView.status = pModel.getStatus();
                vmProductView.productTypeID = (pModel.getProductTypeID() != null) ? pModel.getProductTypeID() : 0;
                lstVMProductView.add(vmProductView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return lstVMProductView;
    }

    public double calculateQuantityOnHand(Integer productID, Integer businessID) throws Exception {
        InventoryTransactionModel searchInventoryTransactionModel = new InventoryTransactionModel();
        List<InventoryTransactionModel> lstInventoryTransactionModel = new ArrayList<>();
        double quantityOnHand = 0;
        double inQuantity = 0;
        double outQuantity = 0;
        try {
            searchInventoryTransactionModel.setBusinessID(businessID);
            searchInventoryTransactionModel.setProductID(productID);
            searchInventoryTransactionModel.setStatus(TillBoxAppEnum.Status.Active.get());
            lstInventoryTransactionModel = this.inventoryTransactionBllManager.getAllByConditions(searchInventoryTransactionModel);
            if (lstInventoryTransactionModel.size() > 0) {
                for (InventoryTransactionModel inventoryTransactionModel : lstInventoryTransactionModel) {
                    if (inventoryTransactionModel.getInQuantity() != null) {
                        if (inventoryTransactionModel.getInQuantity() > 0) {
                            inQuantity += inventoryTransactionModel.getInQuantity();
                        } else if (inventoryTransactionModel.getOutQuantity() > 0) {
                            outQuantity += inventoryTransactionModel.getOutQuantity();
                        }
                    }
                }
            }

            quantityOnHand = inQuantity - outQuantity;
        } catch (Exception ex) {
            log.error("ProductBllManager -> calculateLastPrice got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return quantityOnHand;
    }

    public double calculateLastPrice(Integer productID, Integer businessID) throws Exception {
        InventoryTransactionModel searchInventoryTransactionModel = new InventoryTransactionModel();
        List<InventoryTransactionModel> lstInventoryTransactionModel = new ArrayList<>();
        double lastPrice = 0;
        try {
            searchInventoryTransactionModel.setBusinessID(businessID);
            searchInventoryTransactionModel.setProductID(productID);
            lstInventoryTransactionModel = this.inventoryTransactionBllManager.getAllByConditionWithActive(searchInventoryTransactionModel);

            if (lstInventoryTransactionModel.size() > 0) {
                InventoryTransactionModel inventoryTransactionModel = lstInventoryTransactionModel.get(lstInventoryTransactionModel.size() - 1);
                if (inventoryTransactionModel != null) {
                    lastPrice = (inventoryTransactionModel.getPrice() != null) ? inventoryTransactionModel.getPrice() : 0;
                }
            }
        } catch (Exception ex) {
            log.error("ProductBllManager -> calculateLastPrice got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return lastPrice;
    }

    public double calculateAveragePrice(Integer productID, Integer businessID) throws Exception {
        this.productPriceListBllManager = new ProductPriceListBllManager();
        double averagePrice = 0;
        double totalPrice = 0;
        double totalItemCount = 0;
        InventoryTransactionModel searchInventoryTransactionModel = new InventoryTransactionModel();
        List<InventoryTransactionModel> lstInventoryTransactionModel = new ArrayList<>();
        try {

            searchInventoryTransactionModel.setBusinessID(businessID);
            searchInventoryTransactionModel.setProductID(productID);
            lstInventoryTransactionModel = this.inventoryTransactionBllManager.getAllByConditionWithActive(searchInventoryTransactionModel);
            for (InventoryTransactionModel inventoryTransactionModel : lstInventoryTransactionModel) {

                if (inventoryTransactionModel.getInQuantity() != null) {
                    if (inventoryTransactionModel.getPrice() != null && inventoryTransactionModel.getInQuantity() != null && inventoryTransactionModel.getPrice() > 0 && inventoryTransactionModel.getInQuantity() > 0) {
                        totalPrice += inventoryTransactionModel.getPrice() * inventoryTransactionModel.getInQuantity();
                        totalItemCount += inventoryTransactionModel.getInQuantity();
                    }
                }

            }

//            if (lstInventoryTransactionModel.size() > 0) {
//
//
//            } else {
//                PriceCategoryModel priceCategoryModel = new PriceCategoryModel();
//                priceCategoryModel.setBusinessID(businessID);
//                priceCategoryModel.setDefault(true);
//
//                List<PriceCategoryModel> priceCategoryModels = new ArrayList<>();
//                priceCategoryModels = this.productPriceListBllManager.getAllByConditions(priceCategoryModel);
//                if (priceCategoryModels.size() > 0) {
//
//                    ProductSalesPriceModel productSalesPriceModel = new ProductSalesPriceModel();
//                    productSalesPriceModel.setProductID(productID);
//                    productSalesPriceModel.setBusinessID(businessID);
//                    productSalesPriceModel.setPriceCategoryID(priceCategoryModels.get(0).getPriceCategoryID());
//                    List<ProductSalesPriceModel> productSalesPriceModels=new ArrayList<>();
//                    productSalesPriceModels=this.productSalesPriceBllManager.getAllByConditionWithActive(productSalesPriceModel);
//
//
//                }
//            }


            averagePrice = (totalPrice > 0 && totalItemCount > 0) ? totalPrice / totalItemCount : 0;
        } catch (Exception ex) {
            log.error("ProductBllManager -> calculateAveragePrice got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }

        return averagePrice;
    }

    public ProductModel saveProductOpeningBalance(Integer businessID, Integer productID, Double newOpeningCost, Double newOpeningQuantity) throws Exception {
        ProductModel productModel = new ProductModel();
        List<ProductModel> lstProductModel = new ArrayList<>();
        try {
            productModel.setBusinessID(businessID);
            productModel.setProductID(productID);

            lstProductModel = this.getAllByConditionWithActive(productModel);
            if (lstProductModel.size() == 0) {
                Core.clientMessage.get().messageCode = TillBoxAppConstant.FAILED_ERROR_CODE;
                Core.clientMessage.get().message = MessageConstant.PRODUCT_GET_FAILED;
            } else {
                productModel = lstProductModel.get(0);

                productModel.setOpeningCost(newOpeningCost);
                productModel.setOpeningQuantity(newOpeningQuantity);

                this.update(productModel);
            }

        } catch (Exception ex) {
            log.error("ProductBllManager -> saveProductOpeningBalance got exception : " + ex.getMessage());
            for (Throwable throwable : ex.getSuppressed()) {
                log.error("suppressed: " + throwable);
            }
            throw ex;
        }
        return productModel;
    }
}

package org.spectrumauctions.sats.mechanism.mlca;

import java.math.BigDecimal;
import java.util.Map;

import org.marketdesignresearch.mechlib.mechanism.auctions.mlca.svr.SupportVectorSetup;
import org.marketdesignresearch.mechlib.mechanism.auctions.mlca.svr.kernels.Kernel;
import org.marketdesignresearch.mechlib.mechanism.auctions.mlca.svr.kernels.KernelDotProductExponential;
import org.marketdesignresearch.mechlib.mechanism.auctions.mlca.svr.kernels.KernelDotProductPolynomial;
import org.marketdesignresearch.mechlib.mechanism.auctions.mlca.svr.kernels.KernelGaussian;
import org.marketdesignresearch.mechlib.mechanism.auctions.mlca.svr.kernels.KernelLinear;
import org.marketdesignresearch.mechlib.mechanism.auctions.mlca.svr.kernels.KernelQuadratic;
import org.marketdesignresearch.mechlib.mechanism.auctions.mlca.svr.kernels.KernelType;

public enum SupportVectorSetupFactory {
	INSTANCE;
	
	private Map<KernelType,Kernel> gsvmDefaultKernels = Map.of(
			KernelType.Linear, new KernelLinear(0d, 0.2d), 
			KernelType.Quadratic, new KernelQuadratic(0d, 1d, 0025d),
			KernelType.Polynomial, new KernelDotProductPolynomial(new double[] {0d,1d,0025d}),
			KernelType.Exponential, new KernelDotProductExponential(10d, 10d),
			KernelType.Gaussian, new KernelGaussian(20d, 10d));
	
	private Map<KernelType,Kernel> lsvmDefaultKernels = Map.of(
			KernelType.Linear, new KernelLinear(0d,80d),
			KernelType.Quadratic, new KernelQuadratic(0d, 80d, 0.5d),
			KernelType.Polynomial, new KernelDotProductPolynomial(new double[] {0d,80d,0.5d}),
			KernelType.Exponential, new KernelDotProductExponential(5d, 10d),
			KernelType.Gaussian, new KernelGaussian(10d,10d));
	
	private Map<KernelType,Kernel> mrvmDefaultKernels = Map.of(
			KernelType.Linear, new KernelLinear(0d,100d),
			KernelType.Quadratic, new KernelQuadratic(0d,100d,0.1d),
			KernelType.Polynomial, new KernelDotProductPolynomial(new double[] {0d,100d,0.1d}),
			KernelType.Exponential, new KernelDotProductExponential(100d, 100d),
			KernelType.Gaussian, new KernelGaussian(100d, 100d));
	
	public SupportVectorSetup createDefaultGSVMSupportVectorSetup(KernelType kernelType) {
		return new SupportVectorSetup(3000d, 0.05d, BigDecimal.ONE,this.getRemoveSupportVectors(kernelType),this.getThresholdToRemoveSupportVectors(kernelType), this.gsvmDefaultKernels.get(kernelType));
	}
	
	public SupportVectorSetup createDefaultLSVMSupportVectorSetup(KernelType kernelType) {
		return new SupportVectorSetup(100d,0.0001d,BigDecimal.ONE,this.getRemoveSupportVectors(kernelType),this.getThresholdToRemoveSupportVectors(kernelType),this.lsvmDefaultKernels.get(kernelType));
	}
	
	public SupportVectorSetup createDefaultMRVMSupportVectorSetup(KernelType kernelType) {
		return new SupportVectorSetup(100d,0.0001d,BigDecimal.ONE.divide(BigDecimal.valueOf(100000)),this.getRemoveSupportVectors(kernelType),this.getThresholdToRemoveSupportVectors(kernelType),this.mrvmDefaultKernels.get(kernelType));
	}
	
	/**
	 * For historic reasons eliminiate small support vectors (1e-5) in linear and quadratic kernel SVRs
	 */
	private boolean getRemoveSupportVectors(KernelType type) {
//		if(type.equals(KernelType.Linear) || type.equals(KernelType.Quadratic)) 
//			return true;
		return false;
	}
	
	private double getThresholdToRemoveSupportVectors(KernelType type) {
		//if(type.equals(KernelType.Linear) || type.equals(KernelType.Quadratic)) 
		//	return 1e-5;
		return 1e-9;
	}
	
}

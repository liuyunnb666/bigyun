package com.bigyun.config.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CapabilityModelCompareVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long leftCapabilityId;

    private Long rightCapabilityId;

    private String leftCapabilityCode;

    private String rightCapabilityCode;

    private String leftCapabilityName;

    private String rightCapabilityName;

    private String leftBusinessScene;

    private String rightBusinessScene;

    private String leftCapabilityLayer;

    private String rightCapabilityLayer;

    private String leftConfigType;

    private String rightConfigType;

    private String leftProviderCode;

    private String rightProviderCode;

    private String leftModelCode;

    private String rightModelCode;

    private String leftModelName;

    private String rightModelName;

    private String sameBusinessFlag;

    private String sameTypeFlag;

    private String compatibleFlag;

    private String relationTypeSuggestion;

    private String comparisonSummary;

    private List<String> reasons = new ArrayList<>();

    private List<String> mismatches = new ArrayList<>();

    private List<String> sharedTraits = new ArrayList<>();

    public Long getLeftCapabilityId()
    {
        return leftCapabilityId;
    }

    public void setLeftCapabilityId(Long leftCapabilityId)
    {
        this.leftCapabilityId = leftCapabilityId;
    }

    public Long getRightCapabilityId()
    {
        return rightCapabilityId;
    }

    public void setRightCapabilityId(Long rightCapabilityId)
    {
        this.rightCapabilityId = rightCapabilityId;
    }

    public String getLeftCapabilityCode()
    {
        return leftCapabilityCode;
    }

    public void setLeftCapabilityCode(String leftCapabilityCode)
    {
        this.leftCapabilityCode = leftCapabilityCode;
    }

    public String getRightCapabilityCode()
    {
        return rightCapabilityCode;
    }

    public void setRightCapabilityCode(String rightCapabilityCode)
    {
        this.rightCapabilityCode = rightCapabilityCode;
    }

    public String getLeftCapabilityName()
    {
        return leftCapabilityName;
    }

    public void setLeftCapabilityName(String leftCapabilityName)
    {
        this.leftCapabilityName = leftCapabilityName;
    }

    public String getRightCapabilityName()
    {
        return rightCapabilityName;
    }

    public void setRightCapabilityName(String rightCapabilityName)
    {
        this.rightCapabilityName = rightCapabilityName;
    }

    public String getLeftBusinessScene()
    {
        return leftBusinessScene;
    }

    public void setLeftBusinessScene(String leftBusinessScene)
    {
        this.leftBusinessScene = leftBusinessScene;
    }

    public String getRightBusinessScene()
    {
        return rightBusinessScene;
    }

    public void setRightBusinessScene(String rightBusinessScene)
    {
        this.rightBusinessScene = rightBusinessScene;
    }

    public String getLeftCapabilityLayer()
    {
        return leftCapabilityLayer;
    }

    public void setLeftCapabilityLayer(String leftCapabilityLayer)
    {
        this.leftCapabilityLayer = leftCapabilityLayer;
    }

    public String getRightCapabilityLayer()
    {
        return rightCapabilityLayer;
    }

    public void setRightCapabilityLayer(String rightCapabilityLayer)
    {
        this.rightCapabilityLayer = rightCapabilityLayer;
    }

    public String getLeftConfigType()
    {
        return leftConfigType;
    }

    public void setLeftConfigType(String leftConfigType)
    {
        this.leftConfigType = leftConfigType;
    }

    public String getRightConfigType()
    {
        return rightConfigType;
    }

    public void setRightConfigType(String rightConfigType)
    {
        this.rightConfigType = rightConfigType;
    }

    public String getLeftProviderCode()
    {
        return leftProviderCode;
    }

    public void setLeftProviderCode(String leftProviderCode)
    {
        this.leftProviderCode = leftProviderCode;
    }

    public String getRightProviderCode()
    {
        return rightProviderCode;
    }

    public void setRightProviderCode(String rightProviderCode)
    {
        this.rightProviderCode = rightProviderCode;
    }

    public String getLeftModelCode()
    {
        return leftModelCode;
    }

    public void setLeftModelCode(String leftModelCode)
    {
        this.leftModelCode = leftModelCode;
    }

    public String getRightModelCode()
    {
        return rightModelCode;
    }

    public void setRightModelCode(String rightModelCode)
    {
        this.rightModelCode = rightModelCode;
    }

    public String getLeftModelName()
    {
        return leftModelName;
    }

    public void setLeftModelName(String leftModelName)
    {
        this.leftModelName = leftModelName;
    }

    public String getRightModelName()
    {
        return rightModelName;
    }

    public void setRightModelName(String rightModelName)
    {
        this.rightModelName = rightModelName;
    }

    public String getSameBusinessFlag()
    {
        return sameBusinessFlag;
    }

    public void setSameBusinessFlag(String sameBusinessFlag)
    {
        this.sameBusinessFlag = sameBusinessFlag;
    }

    public String getSameTypeFlag()
    {
        return sameTypeFlag;
    }

    public void setSameTypeFlag(String sameTypeFlag)
    {
        this.sameTypeFlag = sameTypeFlag;
    }

    public String getCompatibleFlag()
    {
        return compatibleFlag;
    }

    public void setCompatibleFlag(String compatibleFlag)
    {
        this.compatibleFlag = compatibleFlag;
    }

    public String getRelationTypeSuggestion()
    {
        return relationTypeSuggestion;
    }

    public void setRelationTypeSuggestion(String relationTypeSuggestion)
    {
        this.relationTypeSuggestion = relationTypeSuggestion;
    }

    public String getComparisonSummary()
    {
        return comparisonSummary;
    }

    public void setComparisonSummary(String comparisonSummary)
    {
        this.comparisonSummary = comparisonSummary;
    }

    public List<String> getReasons()
    {
        return reasons;
    }

    public void setReasons(List<String> reasons)
    {
        this.reasons = reasons == null ? new ArrayList<String>() : reasons;
    }

    public List<String> getMismatches()
    {
        return mismatches;
    }

    public void setMismatches(List<String> mismatches)
    {
        this.mismatches = mismatches == null ? new ArrayList<String>() : mismatches;
    }

    public List<String> getSharedTraits()
    {
        return sharedTraits;
    }

    public void setSharedTraits(List<String> sharedTraits)
    {
        this.sharedTraits = sharedTraits == null ? new ArrayList<String>() : sharedTraits;
    }
}

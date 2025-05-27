package com.naratil.user.dto.signup;

import java.util.List;

public record InterestsDto(
        boolean agreeTerms,
        List<String> keywords,      //
        List<String> regions,       //
        List<String> industries,    //
        String minBudget,           //
        String maxBudget            //
) {}

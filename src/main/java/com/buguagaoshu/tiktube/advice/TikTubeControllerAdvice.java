package com.buguagaoshu.tiktube.advice;

import com.buguagaoshu.tiktube.enums.ReturnCodeEnum;
import com.buguagaoshu.tiktube.exception.InvitationCodeException;
import com.buguagaoshu.tiktube.exception.NeedToCheckEmailException;
import com.buguagaoshu.tiktube.exception.UserNotFoundException;
import com.buguagaoshu.tiktube.exception.VerifyFailedException;
import com.buguagaoshu.tiktube.vo.ResponseDetails;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;





@RestControllerAdvice(basePackages = {"com.buguagaoshu.tiktube.controller"})
public class TikTubeControllerAdvice {

    @ExceptionHandler(value = {Exception.class})
    public ResponseDetails handleException(Exception e) {
        return ResponseDetails.ok(-1, e.getMessage());
    }


    @ExceptionHandler(value = {UserNotFoundException.class})
    public ResponseDetails handleUserException(UserNotFoundException e) {
        return ResponseDetails.ok(ReturnCodeEnum.USER_NOT_FIND).put("data", e.getMessage());
    }

    @ExceptionHandler(value = {VerifyFailedException.class})
    public ResponseDetails handleValidException(VerifyFailedException e) {
        return ResponseDetails.ok(ReturnCodeEnum.VERIFY_FAILED).put("data", e.getMessage());
    }

    @ExceptionHandler(value = {InvitationCodeException.class})
    public ResponseDetails handleInvitationCodeException(InvitationCodeException e) {
        return ResponseDetails.ok(ReturnCodeEnum.INVITATION_ERROR).put("data", e.getMessage());
    }

    @ExceptionHandler(value = {NeedToCheckEmailException.class})
    public ResponseDetails handleInvitationCodeException(NeedToCheckEmailException e) {
        return ResponseDetails.ok(ReturnCodeEnum.CHECK_EMAIL).put("data", e.getMessage());
    }
}

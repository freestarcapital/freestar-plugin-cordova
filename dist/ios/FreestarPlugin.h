//
//  CoolPlugin.h
//  HelloWorld
//
//  Created by Lev Trubov on 10/9/20.
//
//
#import <Cordova/CDV.h>
#import <Foundation/Foundation.h>

@interface FreestarPlugin : CDVPlugin

-(void)SET_USER_PARAMS:(CDVInvokedUrlCommand*)command;

- (void) LOAD_INTERSTITIAL_AD:(CDVInvokedUrlCommand *)command;
- (void) SHOW_INTERSTITIAL_AD:(CDVInvokedUrlCommand *)command;

- (void) LOAD_REWARD_AD:(CDVInvokedUrlCommand *)command;
- (void) SHOW_REWARD_AD:(CDVInvokedUrlCommand *)command;

- (void) CLOSE_BANNER_AD:(CDVInvokedUrlCommand *)command;
- (void) SHOW_BANNER_AD:(CDVInvokedUrlCommand *)command;


@end

//
//  CoolPlugin.m
//  HelloWorld
//
//  Created by Lev Trubov on 10/9/20.
//
//

#import "FreestarPlugin.h"

@import FreestarAds;

#define KEY_AGE @"AGE"
#define KEY_BIRTHDATE    @"BIRTHDATE"
#define KEY_GENDER @"GENDER"
#define KEY_MARITALSTATUS @"MARITALSTATUS"
#define KEY_ETHNICITY @"ETHNICITY"
#define KEY_DMACODE    @"DMACODE"
#define KEY_POSTAL    @"POSTAL"
#define KEY_CURPOSTAL    @"CURPOSTAL"
#define KEY_LATITUDE    @"LATITUDE"
#define KEY_LONGITUDE    @"LONGITUDE"

#define KEY_PLACEMENT @"placement"

@interface CDVInvokedUrlCommand (Freestar)
-(NSString *)extractPlacement;
@end

@implementation CDVInvokedUrlCommand (Freestar)

-(NSString *)extractPlacement {
    NSString *placement = [[self.arguments objectAtIndex:0] valueForKey:KEY_PLACEMENT];
    if(!placement || ![placement isKindOfClass:[NSString class]]) {
        return @"";
    }
    return placement;
}

@end

@interface FreestarPlugin ()
    <FreestarInterstitialDelegate,
    FreestarRewardedDelegate,
    FreestarBannerAdDelegate>

@property NSMutableDictionary *interstitials;
@property NSMutableDictionary *rewarded;
@property NSMutableDictionary *banners;


@property(nonatomic,copy)NSString* callbackId;

@end

@implementation FreestarPlugin

- (void)pluginInitialize {
    NSString *apiKey = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"com.freestar.ios.ads.API_KEY"];
    
    self.interstitials = [[NSMutableDictionary alloc] init];
    self.rewarded = [[NSMutableDictionary alloc] init];
    self.banners = [[NSMutableDictionary alloc] init];
    
    
    [Freestar initWithAdUnitID:apiKey];
}

-(void)SET_TESTMODE_PARAMS:(CDVInvokedUrlCommand*)command {}

-(void)SET_USER_PARAMS:(CDVInvokedUrlCommand*)command {
    [self setDemographics:command];
    [self setLocation:command];
}


- (void) setDemographics:(CDVInvokedUrlCommand *)command {
    NSDictionary* dict = [command.arguments objectAtIndex:0];
    
    NSString* age = [dict valueForKey:KEY_AGE];
    NSString* birthDate = [dict valueForKey:KEY_BIRTHDATE];
    NSString* gender = [dict valueForKey:KEY_GENDER];
    NSString* maritalStatus = [dict valueForKey:KEY_MARITALSTATUS];
    NSString* ethicity = [dict valueForKey:KEY_ETHNICITY];
    
    FreestarDemographics *dem = [Freestar demographics];
    
    if ([self isStringValid:age]){
        dem.age = [age integerValue];
    }
    if ([self isStringValid:birthDate]){
        NSArray<NSString*> *comps = [birthDate componentsSeparatedByString:@"-"];
        if(comps.count >= 3){
            NSInteger year = [comps[0] integerValue];
            NSInteger month = [comps[1] integerValue];
            NSInteger day = [comps[2] integerValue];
            if(year != 0 && month != 0 && day != 0){
                [dem setBirthdayYear:year month:month day:day];
            }
        }
    }
    
    if ([self isStringValid:maritalStatus]){
        if([maritalStatus rangeOfString:@"single" options:NSCaseInsensitiveSearch].location != NSNotFound){
            dem.maritalStatus = FreestarMaritalStatusSingle;
        }else if([maritalStatus rangeOfString:@"married" options:NSCaseInsensitiveSearch].location != NSNotFound){
            dem.maritalStatus = FreestarMaritalStatusMarried;
        }else if([maritalStatus rangeOfString:@"divorced" options:NSCaseInsensitiveSearch].location != NSNotFound){
            dem.maritalStatus = FreestarMaritalStatusDivorced;
        }else if([maritalStatus rangeOfString:@"widowed" options:NSCaseInsensitiveSearch].location != NSNotFound){
            dem.maritalStatus = FreestarMaritalStatusWidowed;
        }else if([maritalStatus rangeOfString:@"separated" options:NSCaseInsensitiveSearch].location != NSNotFound){
            dem.maritalStatus = FreestarMaritalStatusSeparated;
        }else if([maritalStatus rangeOfString:@"other" options:NSCaseInsensitiveSearch].location != NSNotFound){
            dem.maritalStatus = FreestarMaritalStatusOther;
        }
    }
    if ([self isStringValid:gender]){
        NSString *genderString = gender;
        
        NSRange maleRange = [genderString rangeOfString:@"Male" options:NSCaseInsensitiveSearch];
        NSRange feMaleRange = [genderString rangeOfString:@"Female" options:NSCaseInsensitiveSearch];
        
        if (maleRange.location != NSNotFound){
            dem.gender = FreestarGenderMale;
        }else if (feMaleRange.location != NSNotFound){
            dem.gender = FreestarGenderFemale;
        }else if (feMaleRange.location != NSNotFound){
            dem.gender = FreestarGenderOther;
        }
    }
    if ([self isStringValid:ethicity]){
        dem.ethnicity = ethicity;
    }
}

- (void) setLocation:(CDVInvokedUrlCommand *)command {
    NSDictionary* dict = [command.arguments objectAtIndex:0];
    
    NSString* dmacode = [dict valueForKey:KEY_DMACODE];
    NSString* postalCode = [dict valueForKey:KEY_POSTAL];
    NSString* curPostalCode = [dict valueForKey:KEY_CURPOSTAL];
    NSString* latitude = [dict valueForKey:KEY_LATITUDE];
    NSString* lagitude = [dict valueForKey:KEY_LONGITUDE];
    
    FreestarLocation *loc = [Freestar location];
    
    if ([self isStringValid:dmacode]){
        loc.dmacode = dmacode;
    }
    if ([self isStringValid:postalCode]){
        loc.postalcode = postalCode;
    }
    if ([self isStringValid:curPostalCode]){
        loc.currpostal = curPostalCode;
    }
    if ([self isStringValid:latitude] && [self isStringValid:lagitude])
    {
        CLLocation *location = [[CLLocation alloc] initWithLatitude:[latitude doubleValue] longitude:[lagitude doubleValue]];
        
        loc.location = location;
    }
}

- (void) LOAD_INTERSTITIAL_AD:(CDVInvokedUrlCommand *)command {
    
    self.callbackId = command.callbackId;
    
    // Retrieve the JavaScript-created date String from the CDVInvokedUrlCommand instance.
    // When we implement the JavaScript caller to this function, we'll see how we'll
    // pass an array (command.arguments), which will contain a single String.
    NSString * placement = [command extractPlacement];
    FreestarInterstitialAd *ad = [[FreestarInterstitialAd alloc] initWithDelegate:self];
    self.interstitials[placement] = ad;
    [ad loadPlacement:placement];
}

- (void) SHOW_INTERSTITIAL_AD:(CDVInvokedUrlCommand *)command {
    
    self.callbackId = command.callbackId;
    
    NSString * placement  = [command extractPlacement];
    
    FreestarInterstitialAd *ad = self.interstitials[placement];
    
    [ad showFrom:[self visibleViewController:[UIApplication sharedApplication].keyWindow.rootViewController]];
}

#pragma mark - Interstitial Callback

-(NSString *)jsEventString:(NSString *)event params:(NSDictionary *)params {
    return [NSString stringWithFormat:@"javascript:cordova.fireDocumentEvent('%@',%@);",
            event,
            [[NSString alloc] initWithData:[NSJSONSerialization dataWithJSONObject:params options:0 error:nil] encoding:NSUTF8StringEncoding]
            ];
}

-(void)freestarInterstitialLoaded:(FreestarInterstitialAd *)ad {
    [self.commandDelegate runInBackground:^{
        [self.commandDelegate evalJs:[self jsEventString:@"onInterstitialLoaded" params:@{@"placement": ad.placement}]];
    }];
}

-(void)freestarInterstitialFailed:(FreestarInterstitialAd *)ad because:(FreestarNoAdReason)reason {
    [self.commandDelegate runInBackground:^{
        [self.commandDelegate evalJs:[self jsEventString:@"onInterstitialFailed" params:@{@"placement": ad.placement}]];
    }];
}

-(void)freestarInterstitialShown:(FreestarInterstitialAd *)ad {
    [self.commandDelegate runInBackground:^{
        [self.commandDelegate evalJs:[self jsEventString:@"onInterstitialShown" params:@{@"placement": ad.placement}]];
    }];
}

-(void)freestarInterstitialClicked:(FreestarInterstitialAd *)ad {
    [self.commandDelegate runInBackground:^{
        [self.commandDelegate evalJs:[self jsEventString:@"onInterstitialClicked" params:@{@"placement": ad.placement}]];
    }];
}

-(void)freestarInterstitialClosed:(FreestarInterstitialAd *)ad {
    [self.commandDelegate runInBackground:^{
        [self.commandDelegate evalJs:[self jsEventString:@"onInterstitialDismissed" params:@{@"placement": ad.placement}]];
    }];
}


- (void)LOAD_REWARD_AD:(CDVInvokedUrlCommand *)command {
    
    self.callbackId = command.callbackId;
    
    NSString * placement  = [command extractPlacement];
    FreestarRewardedAd *ad = [[FreestarRewardedAd alloc] initWithDelegate:self andReward:[FreestarReward blankReward]];
    self.rewarded[placement] = ad;
    [ad loadPlacement:placement];
}

- (void)SHOW_REWARD_AD:(CDVInvokedUrlCommand *)command {
    NSString * placement  = [command extractPlacement];
    
    FreestarRewardedAd *ad = self.rewarded[placement];
    
    FreestarReward *rew = [FreestarReward blankReward];
    rew.secretKey = [[command.arguments objectAtIndex:0] valueForKey:@"SECRET"];
    rew.userID = [[command.arguments objectAtIndex:0] valueForKey:@"USERID"];
    rew.rewardName = [[command.arguments objectAtIndex:0] valueForKey:@"REWARDNAME"];
    rew.rewardAmount = [[[command.arguments objectAtIndex:0] valueForKey:@"REWARDAMOUNT"] integerValue];
    ad.reward = rew;

    
    [ad showFrom:[self visibleViewController:[UIApplication sharedApplication].keyWindow.rootViewController]];
    
}

#pragma Mark - Rewarded Callbacks

-(void)freestarRewardedLoaded:(FreestarRewardedAd *)ad {
    [self.commandDelegate runInBackground:^{
        [self.commandDelegate evalJs:[self jsEventString:@"onRewardedVideoLoaded" params:@{@"placement": ad.placement}]];
    }];
}

-(void)freestarRewardedFailed:(FreestarRewardedAd *)ad because:(FreestarNoAdReason)reason {
    [self.commandDelegate runInBackground:^{
        [self.commandDelegate evalJs:[self jsEventString:@"onRewardedVideoFailed" params:@{@"placement": ad.placement}]];
    }];
}

-(void)freestarRewardedShown:(FreestarRewardedAd *)ad {
    [self.commandDelegate runInBackground:^{
        [self.commandDelegate evalJs:[self jsEventString:@"onRewardedVideoShown" params:@{@"placement": ad.placement}]];
    }];
}

-(void)freestarRewardedClosed:(FreestarRewardedAd *)ad {
    [self.commandDelegate runInBackground:^{
        [self.commandDelegate evalJs:[self jsEventString:@"onRewardedVideoDismissed" params:@{@"placement": ad.placement}]];
    }];
}

-(void)freestarRewardedFailedToStart:(FreestarRewardedAd *)ad because:(FreestarNoAdReason)reason {
    [self.commandDelegate runInBackground:^{
        [self.commandDelegate evalJs:[self jsEventString:@"onRewardedVideoShownError" params:@{@"placement": ad.placement}]];
    }];
}

-(void)freestarRewardedAd:(FreestarRewardedAd *)ad received:(NSString *)rewardName amount:(NSInteger)rewardAmount {
    [self.commandDelegate runInBackground:^{
        [self.commandDelegate evalJs:[self jsEventString:@"onRewardedVideoCompleted" params:@{@"placement": ad.placement}]];
    }];
}

- (UIViewController *)visibleViewController:(UIViewController *)rootViewController {
    if (rootViewController.presentedViewController == nil)
    {
        return rootViewController;
    }
    if ([rootViewController.presentedViewController isKindOfClass:[UINavigationController class]])
    {
        UINavigationController *navigationController = (UINavigationController *)rootViewController.presentedViewController;
        UIViewController *lastViewController = [[navigationController viewControllers] lastObject];
        
        return [self visibleViewController:lastViewController];
    }
    if ([rootViewController.presentedViewController isKindOfClass:[UITabBarController class]])
    {
        UITabBarController *tabBarController = (UITabBarController *)rootViewController.presentedViewController;
        UIViewController *selectedViewController = tabBarController.selectedViewController;
        
        return [self visibleViewController:selectedViewController];
    }
    
    UIViewController *presentedViewController = (UIViewController *)rootViewController.presentedViewController;
    
    return [self visibleViewController:presentedViewController];
}

#pragma mark - banners


- (void) SHOW_BANNER_AD:(CDVInvokedUrlCommand *)command {
    NSString * placement  = [command extractPlacement];
    
    NSInteger adSize = [[[command.arguments objectAtIndex:0] valueForKey:@"banner_ad_size"] integerValue];
    NSInteger adPosition = [[[command.arguments objectAtIndex:0] valueForKey:@"banner_ad_position"] integerValue];

    
    FreestarBannerAdSize size = adSize == 0 ? FreestarBanner320x50 : FreestarBanner300x250;
    
    FreestarBannerAd *ad = [[FreestarBannerAd alloc] initWithDelegate:self andSize:size];
    ad.tag = adPosition;
    
    self.banners[placement] = ad;
    [ad loadPlacement:placement];
}

- (void) CLOSE_BANNER_AD:(CDVInvokedUrlCommand *)command {
    NSString * placement  = [command extractPlacement];
    
    FreestarBannerAd *ad = self.banners[placement];
    [ad removeFromSuperview];
    self.banners[placement] = nil;
}

-(void)freestarBannerLoaded:(FreestarBannerAd *)ad {
    UIWindow *kw = nil;
    for(UIWindow *w in UIApplication.sharedApplication.windows) {
        if ([w isKeyWindow]) {
            kw = w;
            break;
        }
    }
    
    CGFloat vPos;
    if (ad.tag == 0) { //bottom
        vPos = kw.bounds.size.height - ad.bounds.size.height/2;
    } else if (ad.tag == 1) { //middle
        vPos = CGRectGetMidY(kw.bounds);
    } else { //top
        vPos = ad.bounds.size.height/2;
    }
    
    ad.center = CGPointMake(CGRectGetMidX(kw.bounds), vPos);
    [kw addSubview:ad];
    
    [self.commandDelegate runInBackground:^{
        [self.commandDelegate evalJs:[self jsEventString:@"onBannerAdShown" params:@{@"placement": [ad valueForKeyPath:@"ad.placement"]}]];
    }];
    
}

-(void)freestarBannerFailed:(FreestarBannerAd *)ad because:(FreestarNoAdReason)reason {
    [self.commandDelegate runInBackground:^{
        [self.commandDelegate evalJs:[self jsEventString:@"onBannerAdFailed" params:@{@"placement": [ad valueForKeyPath:@"ad.placement"]}]];
    }];
}

-(void)freestarBannerShown:(FreestarBannerAd *)ad {
    [self.commandDelegate runInBackground:^{
        [self.commandDelegate evalJs:[self jsEventString:@"onBannerAdShown" params:@{@"placement": [ad valueForKeyPath:@"ad.placement"]}]];
    }];
}

-(void)freestarBannerClicked:(FreestarBannerAd *)ad {
    [self.commandDelegate runInBackground:^{
        [self.commandDelegate evalJs:[self jsEventString:@"onBannerAdClicked" params:@{@"placement": [ad valueForKeyPath:@"ad.placement"]}]];
    }];
}

-(void)freestarBannerClosed:(FreestarBannerAd *)ad {
    [self.commandDelegate runInBackground:^{
        [self.commandDelegate evalJs:[self jsEventString:@"onBannerAdDismissed" params:@{@"placement": [ad valueForKeyPath:@"ad.placement"]}]];
    }];
}


- (BOOL)isStringValid:(NSString*)string {
    if (!string)
    {
        return NO;
    }
    else if ([string isKindOfClass:[NSNull class]])
    {
        return NO;
    }
    else if (string.length == 0)
    {
        return NO;
    }
    return YES;
}

@end



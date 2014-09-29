'use strict';

angular.module('ice.wor.controller', [])
    .controller('WorContentController', function ($rootScope, $scope, $location, $modal, $cookieStore, $stateParams, WebOfRegistries) {
        $scope.selectedPartner = $stateParams.partner;
        $scope.loadingPage = true;
        var wor = WebOfRegistries();
        $scope.queryParams = {limit:15, offset:0, sort:'created', partnerId:$stateParams.partner};
        $scope.webResults = undefined;

        // init: retrieve first page of results
        console.log("WorContentController");
        wor.getPublicEntries($scope.queryParams, function (result) {
            $scope.loadingPage = false;
            $scope.webResults = result;
            console.log(result);
        }, function (error) {
            console.error(error);
            $scope.loadingPage = false;
        });

        $scope.currentPage = 1;
        $scope.maxSize = 5;

        $scope.showEntryDetails = function (entry, index) {
//            if (!$scope.queryParams.offset) {
//                $scope.queryParams.offset = index;
//            }
////            console.log("/web/" + $scope.webResults.registryPartner.id + "/entry/" + entry.recordId);
//            $location.path("/web/" + $scope.webResults.registryPartner.id + "/entry/" + entry.id);
        };

        $scope.setPage = function (pageNo) {
            if (pageNo == undefined || isNaN(pageNo))
                pageNo = 1;

            $scope.loadingPage = true;
            $scope.queryParams.offset = (pageNo - 1) * 15;

            wor.getPublicEntries($scope.queryParams, function (result) {
                $scope.webResults = result;
                $scope.loadingPage = false;
            }, function (error) {
                console.error(error);
                $scope.loadingPage = false;
                $scope.webResults = undefined;
            });
        };

        $scope.sort = function (sortType) {
            $scope.webResults = null;
            $scope.queryParams.sort = sortType;
            $scope.queryParams.offset = 0;
            $scope.queryParams.asc = !$scope.queryParams.asc;
            $scope.loadingPage = false;

            wor.getPublicEntries($scope.queryParams, function (result) {
                $scope.loadingPage = false;
                $scope.webResults = result;
                $scope.currentPage = 1;
            }, function (error) {
                console.log(error);
                $scope.webResults = null;
                $scope.loadingPage = false;
            });
        };

        $scope.tooltipDetails = function (entry) {
            $scope.currentTooltip = entry;
        };
    })
    .controller('WorFolderContentController', function ($scope, $stateParams, Remote) {
        var id;
        $scope.remoteRetrieveError = undefined;

        if ($stateParams.folderId === undefined)
            id = $scope.partnerId;
        else
            id = $stateParams.folderId;

        Remote().getFolderEntries({folderId:id, id:$stateParams.partner}, function (result) {
//        console.log("result", result.entries, result === null);
            $scope.selectedPartnerFolder = result;
        }, function (error) {
            console.error(error);
            $scope.selectedPartnerFolder = undefined;
            $scope.remoteRetrieveError = true;
        });
    })
    .controller('WebOfRegistriesDetailController',function ($scope, $cookieStore, $location, $stateParams) {
        var sessionId = $cookieStore.get("sessionId");

        $scope.selectRemotePartnerFolder = function (folder) {
            $scope.partnerId = $stateParams.partner;
            $location.path('/web/' + $stateParams.partner + "/folder/" + folder.id);
        };
    }).controller('WebOfRegistriesController', function ($rootScope, $scope, $location, $modal, $cookieStore, $stateParams, WebOfRegistries, Remote, Settings) {
        var setting = Settings($cookieStore.get("sessionId"));

        // retrieve web of registries partners
        $scope.wor = undefined;
        $scope.isWorEnabled = false;
        setting.getSetting({}, {key:'JOIN_WEB_OF_REGISTRIES'}, function (result) {
            console.log(result);
            $scope.isWorEnabled = result.value === "yes";
        });

        var wor = WebOfRegistries();
//    $scope.getPartners = function(approveOnly) {
        wor.query({approved_only:false}, function (result) {
            $scope.wor = result;
        });
//    };

        $scope.enableDisableWor = function () {
            var value = $scope.isWorEnabled ? 'no' : 'yes';
            setting.update({}, {key:'JOIN_WEB_OF_REGISTRIES', value:value},
                function (result) {
                    $scope.isWorEnabled = result.value === 'yes';
                    $rootScope.settings['JOIN_WEB_OF_REGISTRIES'] = $scope.isWorEnabled ? "yes" : "no";
                }, function (error) {

                });

//            setting.update({}, newSetting, function (result) {
//                newSetting.key = visualKey;
//                newSetting.value = result.value;
//                newSetting.editMode = false;
//            });

        };

        $scope.newPartner = undefined;
        $scope.addPartner = function () {
            wor.addPartner({}, $scope.newPartner, function (result) {
                $scope.wor = result;
                $scope.showAddRegistryForm = false;
                $scope.newPartner = undefined;
            });
        };

        $scope.removePartner = function (partner, index) {
            wor.removePartner({url:partner.url}, function (result) {
                $scope.wor.partners.splice(index, 1);
            });
        };

        $scope.approvePartner = function (partner, index) {
            partner.status = 'APPROVED';
            wor.updatePartner({url:partner.url}, partner, function (result) {

            });
        };

        $scope.selectPartner = function (partner) {
            $location.path("/web/" + partner.id);
            $scope.selectedPartner = partner.id;
            var remote = Remote();
            remote.publicFolders({id:partner.id}, function (result) {
                console.log(result);
                $scope.selectedPartnerFolders = result;
            });
        }
    })
    .controller('WebOfRegistriesMenuController', function ($scope, $location, $modal, $cookieStore, $stateParams, WebOfRegistries, Remote) {
        // retrieve web of registries partners
        $scope.wor = undefined;
        var wor = WebOfRegistries();
        wor.query({approved_only:true}, function (result) {
            $scope.wor = result;
        });

        // retrieves public folders for specified registry and re-directs
        $scope.selectPartner = function (partner) {
            $location.path("/web/" + partner.id);
            $scope.selectedPartner = partner.id;
            var remote = Remote();
            remote.publicFolders({id:partner.id}, function (result) {
                $scope.selectedPartnerFolders = result;
            }, function (error) {
                console.error(error);
            });
        }
    })
    .controller('WorEntryController', function ($scope, WebOfRegistries, $stateParams, EntryService) {
        var web = WebOfRegistries();
        $scope.notFound = undefined;
        $scope.remoteEntry = undefined;

        web.getPublicEntry({partnerId:$stateParams.partner, entryId:$stateParams.entryId}, function (result) {
            $scope.remoteEntry = EntryService.convertToUIForm(result);
            $scope.entryFields = EntryService.getFieldsForType(result.type.toLowerCase());
        }, function (error) {
            console.error(error);
            if (error)
                $scope.notFound = true;
        });

        var menuSubDetails = $scope.subDetails = [
            {url:'/views/wor/entry/1.html', display:'General Information', isPrivileged:false, icon:'fa-exclamation-circle'},
            {id:'comments', url:'/views/wor/entry/2.html', display:'Comments', isPrivileged:false, countName:'commentCount', icon:'fa-comments-o'},
        ];

        $scope.showSelection = function (index) {
            angular.forEach(menuSubDetails, function (details) {
                details.selected = false;
            });
            menuSubDetails[index].selected = true;
            $scope.selection = menuSubDetails[index].url;
//            if (menuSubDetails[index].id) {
//                $location.path("/entry/" + $stateParams.id + "/" + menuSubDetails[index].id);
//            } else {
//                $location.path("/entry/" + $stateParams.id);
//            }
        };

        // check if a selection has been made
        var menuOption = $stateParams.option;
        if (menuOption === undefined) {
            $scope.selection = menuSubDetails[0].url;
            menuSubDetails[0].selected = true;
        } else {
            menuSubDetails[0].selected = false;
            for (var i = 1; i < menuSubDetails.length; i += 1) {
                if (menuSubDetails[i].id === menuOption) {
                    $scope.selection = menuSubDetails[i].url;
                    menuSubDetails[i].selected = true;
                    break;
                }
            }

            if ($scope.selection === undefined) {
                $scope.selection = menuSubDetails[0].url;
                menuSubDetails[0].selected = true;
            }
        }
    })
;
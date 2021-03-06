var todo = angular.module('todo');
todo.controller('loginController', function($scope, loginService, $location) {
	$scope.user = {};

	// for logging user
	$scope.loginUser = function() {
		var httpLogin = loginService.loginuser($scope.user);
		/* localStorage.setItem(); */
		httpLogin.then(function(response) {
			// success logic
			localStorage.setItem('accessToken', JSON
					.stringify(response.data.accessToken));
			localStorage.setItem('refreshToken', JSON
					.stringify(response.data.refreshToken));
			console.log("Tokens are: " + JSON.stringify(response.data));
			console.log('Login success');
			alert('login success');
			$location.path('/home');
		}, function(response) {
			// failure logic
			console.log('Login failed');
			$scope.custom = true;
			$scope.toggleCustom = function() {
				$scope.custom === false ? true : false;
			};
			// $location.path('/login');
		});

	}

	$scope.fbLogin = function() {
		var fblogin = loginService.fbLogin($scope.user);
		fblogin.then(function(response) {
			// success logic
			alert('fb success');
			$location.path('/home');
		}), function(response) {
			alert('fb fail');
			$location.path('/login');
		}
	}

	$scope.gLogin = function() {
		var glogin = loginService.gLogin($scope.user);
		glogin.then(function(response) {
			// success logic
			alert('google success');
			$location.path('/home');
		}), function(response) {
			alert('google fail');
			$location.path('/login');
		}
	}

	// for registration
	$scope.redirectToRegistration = function() {
		$location.path('/registration');

	}

	// for forgot passoword
	$scope.redirectToForgotPassword = function() {
		$location.path('/forgotpassword');
	}

})
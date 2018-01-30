function Calendar() {
    this.version = '0.0.1';
}

/**
 * add
 *
 * @param {Function} successCallback The function to call when the heading data is available
 * @param {Function} errorCallback The function to call when there is an error getting the heading data. (OPTIONAL)
 */
Calendar.prototype.add = function (title, desc, ts, prior, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, 'Calendar', 'add', [title, desc, ts, prior]);
};

module.exports = new Calendar();


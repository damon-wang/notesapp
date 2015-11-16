
var gulp = require('gulp');
var less = require('gulp-less');
var gutil = require("gulp-util");
var sourcemaps = require('gulp-sourcemaps');
var autoprefixer = require('gulp-autoprefixer');

var onError = function (err) {
    gutil.log(gutil.colors.red("ERROR", 'less compile'), err);
    this.emit("end", new gutil.PluginError('less compile', err, { showStack: true }));
};

gulp.task('css', function () {
    gulp.src('./resources/style/main.less')
    .pipe(sourcemaps.init())
    .pipe(less())
    .on('error', onError)
    .pipe(autoprefixer({
        // browsers: ['last 2 versions'],
        cascade: false
    }))
    .pipe(sourcemaps.write())
    .pipe(gulp.dest('./resources/public/css/'));
});

// http://stackoverflow.com/questions/23953779/
gulp.task('watch', function() {
    gulp.watch('./resources/style/' + '*.less', ['css']);  // Watch all the .less files, then run the less task
});

gulp.task('default', ['css', 'watch']);
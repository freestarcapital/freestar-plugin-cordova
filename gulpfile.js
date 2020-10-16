const { src, dest, series } = require('gulp');
const gulp = require('gulp'),
    path = require('path'),
    merge = require('merge-stream'),
    clean = require('gulp-clean')

const DIST_DIR = "./dist";
const FOLDERS = ["ios", "www"]
const FILES = [
    "android_dependencies.gradle",
    "package.json",
    "package-lock.json",
    "plugin.xml",
    "README.md"
]
function clean_dist() {
    return gulp.src(DIST_DIR, {read: false, allowEmpty: true})
        .pipe(clean())
}

function build() {
    const folders_tasks = FOLDERS.map(function(element){

        return gulp.src(element + "/**/*")
            .pipe(gulp.dest(DIST_DIR+ '/' + element +"/"))
    });
    const files_task = FILES.map(function(element){
        console.log(element)
        return gulp.src(element)
            .pipe(gulp.dest(DIST_DIR))
    });
    return merge(folders_tasks,files_task)
}
exports.build = build
exports.clean = clean_dist
exports.default = series(clean_dist, build);
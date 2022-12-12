#!/usr/bin/perl -w
use strict;
use utf8;

my @a = `ps aux | grep SlowWorker`;
chomp @a;
foreach(@a)
  {
	  if($_ !~ /grep/)
	    {
          my @b = split(/ /, $_);
          foreach my $bit(@b){
            if($bit !~ / /)
              {
                if($bit =~ /\d/){
                  `kill -9 $bit`; last;
                }
              }
          } 
		}
  }
  
@a = `ps aux | grep 'run tasks; exec bash'`;
chomp @a;
foreach(@a)
  {
	  if($_ !~ /grep/)
	    {
          my @b = split(/ /, $_);
          foreach my $bit(@b){
            if($bit !~ / /)
              {
                if($bit =~ /\d/){
                  `kill -9 $bit`; last;
                }
              }
          } 
		}
  }
  
1;
